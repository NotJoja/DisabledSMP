package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import java.util.List;

public class Toothless extends DisabilityHandler {

    public Toothless() {
        this.enName = "Toothless";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You can only eat soft food",
                "or soup properly"
        );

        this.deName = "Zahnlos";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du kannst nur weiches Essen",
                "oder Suppen richtig essen"
        );

        initIcons();
    }
}
