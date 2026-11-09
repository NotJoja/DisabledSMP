package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import de.joja.disabledSMP.utils.DConfig;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public class Overweight extends DisabilityHandler {

    Set<UUID> explosionImmune = new HashSet<>();

    /**
     * Players that just set off the explosion: it throws them up, so they come down again and would take fall damage
     * from that. They are safe until they land (or this many ticks have passed).
     */
    private final Set<UUID> fallImmune = new HashSet<>();
    private static final long FALL_IMMUNE_MAX_TICKS = 100;

    /** How long the player can't be hurt after the explosion went off (the shock wave, the first moments). */
    private static final long EXPLOSION_IMMUNE_TICKS = 10;

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

                player.setExhaustion(player.getExhaustion() + (float) DConfig.OVERWEIGHT_HUNGER_DRAIN);
            }
        }, 20L, 20L);
    }



    @EventHandler
    public void onFall(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
            return;
        if (!hasDis(player, Disability.OVERWEIGHT))
            return;

        // landing after being thrown up by the explosion
        if (fallImmune.contains(player.getUniqueId()) || explosionImmune.contains(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        float fall = player.getFallDistance();
        if (fall < DConfig.OVERWEIGHT_EXPLOSION_MIN_FALL)
            return;

        event.setCancelled(true);

        explosionImmune.add(player.getUniqueId());
        // nothing can hurt the player while the explosion goes off (the explosion damage is also cancelled below, but
        // this doesn't depend on which damage cause the server uses for it)
        boolean wasInvulnerable = player.isInvulnerable();
        player.setInvulnerable(true);
        invulnerableBefore.put(player.getUniqueId(), wasInvulnerable);
        plugin.advancementManager.setOverweightExploder(player);
        try {
            player.getWorld().createExplosion(
                    player.getLocation(),
                    fall / 6,
                    false,
                    true
            );
        } finally {
            plugin.advancementManager.setOverweightExploder(null);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            explosionImmune.remove(player.getUniqueId());
            restoreInvulnerable(player);
        }, EXPLOSION_IMMUNE_TICKS);

        startFallImmunity(player);
    }

    /** What the invulnerable flag of the players was before the explosion made them invulnerable for a moment. */
    private final Map<UUID, Boolean> invulnerableBefore = new HashMap<>();

    private void restoreInvulnerable(Player player) {
        Boolean before = invulnerableBefore.remove(player.getUniqueId());
        if (before != null)
            player.setInvulnerable(before);
    }

    /** Leaving within that moment must not save the player as invulnerable. */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        restoreInvulnerable(event.getPlayer());
    }

    /** No fall damage until the player is back on the ground after the explosion (or after a few seconds). */
    private void startFallImmunity(Player player) {
        UUID uuid = player.getUniqueId();
        fallImmune.add(uuid);
        player.setFallDistance(0);

        int[] ticks = {0};
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            ticks[0] += 2;
            // the first ticks they are still on the ground (the explosion hasn't thrown them up yet)
            boolean landed = ticks[0] > 10 && player.isOnGround();
            if (!player.isOnline() || landed || ticks[0] >= FALL_IMMUNE_MAX_TICKS) {
                // one more tick: the landing damage comes in the same tick the player touches the ground
                Bukkit.getScheduler().runTaskLater(plugin, () -> fallImmune.remove(uuid), 2L);
                task.cancel();
            }
        }, 2L, 2L);
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
        Objects.requireNonNull(player.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(DConfig.OVERWEIGHT_MOVEMENT_SPEED);
        Objects.requireNonNull(player.getAttribute(Attribute.JUMP_STRENGTH)).setBaseValue(DConfig.OVERWEIGHT_JUMP_STRENGTH);
    }

    @Override
    public void removeFromPlayer(Player player) {
        Objects.requireNonNull(player.getAttribute(Attribute.MOVEMENT_SPEED)).setBaseValue(0.1);
        Objects.requireNonNull(player.getAttribute(Attribute.JUMP_STRENGTH)).setBaseValue(0.42);
    }

}

