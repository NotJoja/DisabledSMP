package de.joja.disabledSMP.listeners;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.utils.DConfig;
import de.joja.disabledSMP.utils.PlayerAttack;
import de.joja.disabledSMP.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.utils.DConfig.MAX_KILL_TIME_MS;
import static de.joja.disabledSMP.utils.Utils.random;

public class KillListener implements Listener {

    Map<UUID, PlayerAttack> lastAttackMap = new HashMap<>();

    @EventHandler
    public void onAttacked(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Player damagedPlayer))
            return;

        Entity damager = event.getDamager();

        if (damager instanceof Player attackerPlayer) {
            lastAttackMap.put(damagedPlayer.getUniqueId(), new PlayerAttack(attackerPlayer.getUniqueId(), System.currentTimeMillis()));
            return;
        }

        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player attackerPlayer)
            lastAttackMap.put(damagedPlayer.getUniqueId(), new PlayerAttack(attackerPlayer.getUniqueId(), System.currentTimeMillis()));
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        Player died = event.getEntity();
        Player killer = died.getKiller();

        if (killer != null) {
            onPlayerKilledPlayer(died, killer);
            lastAttackMap.remove(died.getUniqueId());
            return;
        }

        PlayerAttack last = lastAttackMap.get(died.getUniqueId());
        if (last == null)
            return;

        if (System.currentTimeMillis() - last.getTime() <= MAX_KILL_TIME_MS) {
            killer = Bukkit.getPlayer(last.getAttackerUUID());
            // the attacker may have logged off in the meantime
            if (killer != null)
                onPlayerKilledPlayer(died, killer);
            lastAttackMap.remove(died.getUniqueId());
            return;
        }

        lastAttackMap.remove(died.getUniqueId());
    }

    public void onPlayerKilledPlayer(Player diedPlayer, Player killerPlayer) {
        if (diedPlayer == killerPlayer)
            return;
        List<Disability> diedPlayerDisabilities = plugin.disabilityMap.get(diedPlayer.getUniqueId());
        List<Disability> killerPlayerDisabilities = plugin.disabilityMap.get(killerPlayer.getUniqueId());
        if (diedPlayerDisabilities == null || killerPlayerDisabilities == null)
            return;
        if (killerPlayerDisabilities.isEmpty()) {
            notifyFailed(killerPlayer, diedPlayer, msg("fail.no_disabilities", "You have no disabilities to pass on."));
            return;
        }

        // only disabilities the victim doesn't have yet can be passed on (no duplicates)
        List<Disability> transferable = new ArrayList<>(killerPlayerDisabilities);
        transferable.removeAll(diedPlayerDisabilities);
        transferable.removeIf(dis -> !dis.isEnabled());
        if (transferable.isEmpty()) {
            DConfig.debug("Transfer skipped: " + diedPlayer.getName() + " already has all of them");
            notifyFailed(killerPlayer, diedPlayer, msg("fail.has_all",
                    "%s already has all of your disabilities, so nothing was passed on.", diedPlayer.name()));
            return;
        }

        // the minimum / maximum amount of disabilities of the server also counts for the transfer
        if (DConfig.MIN_DISABILITIES >= 0 && killerPlayerDisabilities.size() - 1 < DConfig.MIN_DISABILITIES) {
            DConfig.debug("Transfer skipped: " + killerPlayer.getName() + " would go below the minimum");
            notifyFailed(killerPlayer, diedPlayer, msg("fail.min",
                    "You can't pass on a disability: the server minimum of %s would be undercut.",
                    Component.text(DConfig.MIN_DISABILITIES)));
            return;
        }
        if (DConfig.MAX_DISABILITIES >= 0 && diedPlayerDisabilities.size() + 1 > DConfig.MAX_DISABILITIES) {
            DConfig.debug("Transfer skipped: " + diedPlayer.getName() + " would go above the maximum");
            notifyFailed(killerPlayer, diedPlayer, msg("fail.max", "%s already has the maximum of %s disabilities.",
                    diedPlayer.name(), Component.text(DConfig.MAX_DISABILITIES)));
            return;
        }

        Disability transferred = transferable.get(random.nextInt(transferable.size()));
        killerPlayerDisabilities.remove(transferred);
        transferred.handler.removeFromPlayer(killerPlayer);
        diedPlayerDisabilities.add(transferred);
        transferred.handler.addToPlayer(diedPlayer);

        plugin.saveDisabilities(killerPlayer.getUniqueId());
        plugin.saveDisabilities(diedPlayer.getUniqueId());

        Component name = ItemUtils.disabilityName(transferred.enDataName, transferred.enName);
        killerPlayer.sendMessage(msg("passed_on", "You lost %s: you passed it on to %s.", name, diedPlayer.name())
                .color(NamedTextColor.GREEN));
        diedPlayer.sendMessage(msg("received", "You got %s from %s.", name, killerPlayer.name())
                .color(NamedTextColor.RED));
    }

    /** Message text as a translation key: every player sees it in their own client language (lang files of the resource pack). */
    private static Component msg(String key, String fallback, ComponentLike... args) {
        return Component.translatable("message.dismp.kill." + key, fallback, args);
    }

    /** Tells both players that no disability was passed on; the killer gets the reason. */
    private static void notifyFailed(Player killer, Player died, Component reason) {
        killer.sendMessage(reason.color(NamedTextColor.GRAY));
        died.sendMessage(msg("fail.victim", "No disability was passed on to you by %s.", killer.name())
                .color(NamedTextColor.GRAY));
    }
}