package de.joja.disabledSMP.listeners;

import de.joja.disabledSMP.DisabledSMP;
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
import java.util.Map;
import java.util.UUID;

public class KillListener implements Listener {

    long COMBAT_TIMEOUT_MS = 30*1000;

    DisabledSMP plugin;

    Map<UUID, PlayerAttack> lastAttackMap = new HashMap<>();

    public KillListener(DisabledSMP plugin) {
        this.plugin = plugin;
    }

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

        if (System.currentTimeMillis() - last.getTime() <= COMBAT_TIMEOUT_MS) {
            killer = Bukkit.getPlayer(last.getAttackerUUID());
            onPlayerKilledPlayer(died, killer);
            lastAttackMap.remove(died.getUniqueId());
            return;
        }

        lastAttackMap.remove(died.getUniqueId());
    }

    public void onPlayerKilledPlayer(Player diedPlayer, Player killerPlayer) {

    }
}