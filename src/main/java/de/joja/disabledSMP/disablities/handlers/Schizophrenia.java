package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import java.util.List;

public class Schizophrenia extends DisabilityHandler {

    public Schizophrenia() {
        this.enName = "Schizophrenia";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You experience hallucinations",
                "like fake mobs or sounds"
        );

        this.deName = "Schizophrenie";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du erlebst Halluzinationen",
                "wie fake Mobs oder Sounds"
        );
    }
}
