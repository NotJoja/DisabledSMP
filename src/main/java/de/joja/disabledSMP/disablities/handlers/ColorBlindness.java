package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.utils.Lang;
import de.joja.disabledSMP.utils.ResourcePacks;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

public class ColorBlindness extends DisabilityHandler {

    public ColorBlindness() {
        this.enName = "Color Blindness";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You can't see colors"
        );

        this.deName = "Farbenblindheit";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du siehst keine Farben"
        );

        initIcons();
    }

    /** The colorblind pack is added on top of the main pack (which everyone has), not instead of it. */
    @Override
    public void addToPlayer(Player player) {
        ResourcePacks.COLORBLIND.send(player);
    }

    @Override
    public void removeFromPlayer(Player player) {
        ResourcePacks.COLORBLIND.remove(player);
    }
}
