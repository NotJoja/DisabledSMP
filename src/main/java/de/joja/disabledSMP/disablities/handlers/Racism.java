package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import java.util.List;

public class Racism extends DisabilityHandler {

    public Racism() {
        this.enName = "Racism";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "Touching black blocks or items",
                "hurts you"
        );

        this.deName = "Rassismus";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Schwarze Blöcke oder Items",
                "zu berühren schadet dir"
        );

        initIcons();
    }
}
