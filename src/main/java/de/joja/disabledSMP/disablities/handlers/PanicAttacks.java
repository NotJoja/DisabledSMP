package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import java.util.List;

public class PanicAttacks extends DisabilityHandler {

    public PanicAttacks() {
        this.enName = "Panic Attacks";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "If too many players or mobs are nearby,",
                "items can fall out of your inventory",
                "and you will get blindness and speed"
        );

        this.deName = "Panikattacken";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Wenn zu viele Spieler oder Mobs in deiner Nähe",
                "sind, können Items aus deinem Inventar fallen",
                "und du wirst blind und schneller"
        );

        initIcons();
    }
}
