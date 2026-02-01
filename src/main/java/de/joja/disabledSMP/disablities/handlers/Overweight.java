package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import java.util.List;

public class Overweight extends DisabilityHandler {

    public Overweight() {
        this.enName = "Overweight";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You consume more hunger, move slower",
                "and jump lower"
        );

        this.deName = "Übergewicht";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du verbrauchst mehr Hunger",
                "bist langsamer und springst niedriger"
        );

        initIcons();
    }
}
