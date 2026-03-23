package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.*;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public class Overweight extends DisabilityHandler {

    Set<UUID> explosionImmune = new HashSet<>();

    public Overweight() {
        this.enName = "Overweight";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You consume more hunger, move slower",
                "and jump lower, but if you fall from",
                "high enough, you cause an explosion,",
                "that only hurts others"
        );

        this.deName = "Übergewicht";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du verbrauchst mehr Hunger, bist",
                "langsamer und springst niedriger,",
                "aber wenn du von hoch genug runter",
                "fällst, löst du eine Explosion aus,",
                "die nur Anderen Schaden hinzufügt"
        );

        initIcons();

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!hasDis(player, Disability.OVERWEIGHT))
                    continue;

                player.setExhaustion(player.getExhaustion() + 0.03f);
            }
        }, 20L, 20L);

    }



    @EventHandler
    public void onFall(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;

        float fall = player.getFallDistance();
        if (fall < 18)
            return;

        event.setCancelled(true);

        explosionImmune.add(player.getUniqueId());
        player.getWorld().createExplosion(
                player.getLocation(),
                fall / 6,
                false,
                true
        );

        Bukkit.getScheduler().runTaskLater(plugin, () ->
                explosionImmune.remove(player.getUniqueId()), 10L);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;

        boolean explosion = event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
                            event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION;
        boolean immune = explosionImmune.contains(player.getUniqueId());
        if (explosion && immune)
                event.setCancelled(true);
    }



    @Override
    public void addToPlayer(Player player) {
        Objects.requireNonNull(player.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.08);
        Objects.requireNonNull(player.getAttribute(Attribute.JUMP_STRENGTH)).setBaseValue(0.36);
    }

    @Override
    public void removeFromPlayer(Player player) {
        Objects.requireNonNull(player.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.1);
        Objects.requireNonNull(player.getAttribute(Attribute.JUMP_STRENGTH)).setBaseValue(0.42);
    }

}

