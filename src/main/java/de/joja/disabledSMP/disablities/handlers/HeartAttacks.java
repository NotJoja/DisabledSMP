package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import java.util.List;

public class HeartAttacks extends DisabilityHandler {

    public HeartAttacks() {
        this.enName = "Heart Attacks";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You may randomly suffer sudden damage",
                "up to 8 hearts"
        );

        this.deName = "Herzinfarkte";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du erleidest zufällig Schaden",
                "bis zu 8 Herzen"
        );

        initIcons();
    }
}
