package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import java.util.List;

public class ParalyzedLeg extends DisabilityHandler {

    public ParalyzedLeg() {
        this.enName = "Paralyzed Leg";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You move much slower",
                "but can craft a wheelchair"
        );

        this.deName = "Gelähmtes Bein";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du bewegst dich viel langsamer",
                "kannst aber einen Rollstuhl craften"
        );

        initIcons();
    }
}
