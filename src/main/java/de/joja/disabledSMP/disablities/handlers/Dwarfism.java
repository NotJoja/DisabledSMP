package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import de.joja.disabledSMP.utils.DConfig;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class Dwarfism extends DisabilityHandler {

    public Dwarfism() {
        this.enName = "Dwarfism";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You are smaller and have a reduced reach",
                "and less maximum health"
        );

        this.deName = "Kleinwüchsigkeit";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du bist kleiner und hast eine geringere Reichweite",
                "und weniger maximales Leben"
        );

        initIcons();
    }

    public void addToPlayer(Player player) {

        AttributeInstance scale = player.getAttribute(Attribute.SCALE);
        if (scale != null)
            scale.setBaseValue(DConfig.DWARFISM_SCALE);    // default: 1.0

        AttributeInstance entityRange = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        if (entityRange != null)
            entityRange.setBaseValue(DConfig.DWARFISM_ENTITY_RANGE);    // default: 3.0

        AttributeInstance blockRange = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE);
        if (blockRange != null)
            blockRange.setBaseValue(DConfig.DWARFISM_BLOCK_RANGE);    // default: 4.5

        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth != null)
            maxHealth.setBaseValue(DConfig.DWARFISM_MAX_HEALTH);    // default: 20
    }

    public void removeFromPlayer(Player player) {
        Objects.requireNonNull(player.getAttribute(Attribute.SCALE)).setBaseValue(1.0);
        Objects.requireNonNull(player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE)).setBaseValue(3.0);
        Objects.requireNonNull(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)).setBaseValue(4.5);
        Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(20);
    }
}
