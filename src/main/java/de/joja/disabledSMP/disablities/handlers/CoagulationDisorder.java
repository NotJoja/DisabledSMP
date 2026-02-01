package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import java.util.List;

public class CoagulationDisorder extends DisabilityHandler {

    public CoagulationDisorder() {
        this.enName = "Coagulation Disorder";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "If you get damaged, you won't regenerate,",
                "unless you use bandages"
        );

        this.deName = "Gerinnungsstörung";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du kannst dich nur regenerieren,",
                "wenn du Bandagen benutzt"
        );

        initIcons();
    }
}