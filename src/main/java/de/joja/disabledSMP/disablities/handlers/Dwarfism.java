package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import java.util.List;

public class Dwarfism extends DisabilityHandler {

    public Dwarfism() {
        this.enName = "Dwarfism";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You are smaller and have a reduced reach",
                "and less maximum health"
        );

        this.deName = "Kleinwüchsigkeit";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du bist kleiner und hast eine geringere Reichweite",
                "und weniger maximales Leben"
        );
    }
}
