package de.joja.disabledSMP.listeners;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.utils.DConfig;
import de.joja.disabledSMP.utils.Lang;
import de.joja.disabledSMP.utils.PlayerAttack;
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
        if (diedPlayerDisabilities == null || killerPlayerDisabilities == null || killerPlayerDisabilities.isEmpty())
            return;

        // only disabilities the victim doesn't have yet can be passed on (no duplicates)
        List<Disability> transferable = new ArrayList<>(killerPlayerDisabilities);
        transferable.removeAll(diedPlayerDisabilities);
        if (transferable.isEmpty()) {
            DConfig.debug("Transfer skipped: " + diedPlayer.getName() + " already has all of them");
            return;
        }

        // the minimum / maximum amount of disabilities of the server also counts for the transfer
        if (DConfig.MIN_DISABILITIES >= 0 && killerPlayerDisabilities.size() - 1 < DConfig.MIN_DISABILITIES) {
            DConfig.debug("Transfer skipped: " + killerPlayer.getName() + " would go below the minimum");
            return;
        }
        if (DConfig.MAX_DISABILITIES >= 0 && diedPlayerDisabilities.size() + 1 > DConfig.MAX_DISABILITIES) {
            DConfig.debug("Transfer skipped: " + diedPlayer.getName() + " would go above the maximum");
            return;
        }

        Disability transferred = transferable.get(random.nextInt(transferable.size()));
        killerPlayerDisabilities.remove(transferred);
        transferred.handler.removeFromPlayer(killerPlayer);
        diedPlayerDisabilities.add(transferred);
        transferred.handler.addToPlayer(diedPlayer);

        plugin.saveDisabilities(killerPlayer.getUniqueId());
        plugin.saveDisabilities(diedPlayer.getUniqueId());

        // German-only: there is no English word for this
        if (Lang.of(killerPlayer) == Lang.DE)
            killerPlayer.sendMessage("§7Schadenfreude! Du hast eine Disability an " + diedPlayer.getName() + " weitergegeben.");
    }
}