package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.List;

import static de.joja.disabledSMP.DisabledSMP.plugin;

/**
 * You can only move while jumping.
 *
 *  - on the ground: movement speed is multiplied by 0, so walking does nothing (jumping still works)
 *  - in the air: the game uses a fixed air control (the movement speed doesn't matter there), so the speed is
 *    adjusted through the air drag instead: as slow as a player that sneaks, walks and jumps
 *
 * Both are attribute modifiers, so the client predicts them itself (no rubber banding) and they stack with other
 * disabilities that change the base values (e.g. Overweight).
 */
public class ParalyzedLeg extends DisabilityHandler {

    /**
     * Air drag, 1.0 = vanilla. friction = 1 - (1 - 0.91) * modifier, and the top speed in the air is
     * 0.02 * friction / (1 - friction): 1.0 -> 0.20 (walking + jumping), 2.8 -> 0.05 (about what sneaking + walking +
     * jumping gives you, a jump carries you roughly half a block).
     * Higher = slower, lower (down to 1.0) = faster.
     */
    private static final double AIR_DRAG_MODIFIER = 2.8;

    public ParalyzedLeg() {
        this.enName = "Paralyzed Leg";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You can only move while jumping",
                "a wheelchair helps you get around"
        );

        this.deName = "Gelähmtes Bein";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du kannst dich nur beim Springen bewegen",
                "ein Rollstuhl hilft dir weiter"
        );

        initIcons();
    }

    private static NamespacedKey speedKey() {
        return new NamespacedKey(plugin, "paralyzed_leg_speed");
    }

    private static NamespacedKey dragKey() {
        return new NamespacedKey(plugin, "paralyzed_leg_air_drag");
    }

    @Override
    public void addToPlayer(Player player) {
        // x0 on the ground
        applyModifier(player, Attribute.MOVEMENT_SPEED,
                new AttributeModifier(speedKey(), -1.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1));

        // slow in the air (base 1.0 * AIR_DRAG_MODIFIER)
        applyModifier(player, Attribute.AIR_DRAG_MODIFIER,
                new AttributeModifier(dragKey(), AIR_DRAG_MODIFIER - 1.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
    }

    @Override
    public void removeFromPlayer(Player player) {
        removeModifier(player, Attribute.MOVEMENT_SPEED, speedKey());
        removeModifier(player, Attribute.AIR_DRAG_MODIFIER, dragKey());
    }

    /** Transient: gone after a relog and re-added on join, so nothing stays behind if the disability is gone. */
    private static void applyModifier(Player player, Attribute attribute, AttributeModifier modifier) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null)
            return;

        instance.removeModifier(modifier.getKey());     // addToPlayer can be called more than once
        instance.addTransientModifier(modifier);
    }

    private static void removeModifier(Player player, Attribute attribute, NamespacedKey key) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null)
            instance.removeModifier(key);
    }
}
