package de.joja.disabledSMP.listeners;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.utils.PlayerAttack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.utils.ConfigManager.CONFIG_MAX_KILL_TIME_MS;

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

        if (System.currentTimeMillis() - last.getTime() <= CONFIG_MAX_KILL_TIME_MS) {
            killer = Bukkit.getPlayer(last.getAttackerUUID());
            onPlayerKilledPlayer(died, killer);
            lastAttackMap.remove(died.getUniqueId());
            return;
        }

        lastAttackMap.remove(died.getUniqueId());
    }

    public void onPlayerKilledPlayer(Player diedPlayer, Player killerPlayer) {
        if (diedPlayer == killerPlayer)
            return;
        List<Disability> diedPlayerDisabilities = plugin.disManager.disabilityMap.get(diedPlayer.getUniqueId());
        List<Disability> killerPlayerDisabilities = plugin.disManager.disabilityMap.get(killerPlayer.getUniqueId());
        diedPlayerDisabilities.add(killerPlayerDisabilities.getLast());
        killerPlayerDisabilities.removeLast();
    }
}