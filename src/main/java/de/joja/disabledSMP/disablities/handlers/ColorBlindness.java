package de.joja.disabledSMP.disablities.handlers;

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

    @Override
    public void addToPlayer(Player player) {
        player.setResourcePack(
                "https://raw.githubusercontent.com/NotJoja/DisabledSMP/main/src/main/resources/ColorBlindness.zip",
                null,
                Component.text("Every colorblind player must use this resource pack!"),
                true
        );
    }

    @Override
    public void removeFromPlayer(Player player) {
        player.setResourcePack(
                "https://raw.githubusercontent.com/NotJoja/DisabledSMP/main/src/main/resources/DisabledSMP.zip",
                null,
                Component.text("This server requires the DisabledSMP resource pack!"),
                true
        );
    }
}
