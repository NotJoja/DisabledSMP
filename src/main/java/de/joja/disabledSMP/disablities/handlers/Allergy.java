package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import net.kyori.adventure.key.Key;

import java.util.List;

public class Allergy extends DisabilityHandler {

    public Allergy() {
        this.enName = "Allergy";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "A certain food will harm",
                "and weaken you nearby"
        );

        this.deName = "Allergie";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Ein bestimmtes Essen schadet",
                "dir stark in deiner Nähe"
        );

        this.icon = Key.key("disabledsmp", "allergy");
        this.grayIcon = Key.key("disabledsmp", "gray_allergy");
    }
}
