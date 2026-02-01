package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import java.util.List;

public class ColorBlindness extends DisabilityHandler {

    public ColorBlindness() {
        this.enName = "Color Blindness";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "Certain blocks or items",
                "appear indistinguishable"
        );

        this.deName = "Farbenblindheit";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Bestimmte Blöcke oder Items",
                "sehen für dich gleich aus"
        );

        initIcons();
    }
}
