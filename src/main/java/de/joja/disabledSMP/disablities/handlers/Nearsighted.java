package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import java.util.List;

public class Nearsighted extends DisabilityHandler {

    public Nearsighted() {
        this.enName = "Nearsighted";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You have a reduced view distance"
        );

        this.deName = "Kurzsichtigkeit";
        this.deDataName = toDataName(enName);
        this.deDescription = List.of(
                "Du hast eine verringerte Sichtweite"
        );

        initIcons();
    }
}
