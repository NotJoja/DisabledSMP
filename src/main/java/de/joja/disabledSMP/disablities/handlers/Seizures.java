package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import java.util.List;

public class Seizures extends DisabilityHandler {

    public Seizures() {
        this.enName = "Seizures";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "Your crosshair shakes",
                "uncontrollably sometimes"
        );

        this.deName = "Zitteranfälle";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Dein Crosshair zittert",
                "manchmal extrem"
        );

        initIcons();
    }
}
