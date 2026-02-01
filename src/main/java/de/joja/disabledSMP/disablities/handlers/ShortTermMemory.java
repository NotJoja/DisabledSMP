package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import java.util.List;

public class ShortTermMemory extends DisabilityHandler {

    public ShortTermMemory() {
        this.enName = "Short-Term Memory";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "Inventory items may temporarily change",
                "their name, texture or position"
        );

        this.deName = "Kurzzeitgedächtnis";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Items in deinem Inventar können temporär",
                "Aussehen, Namen oder Position ändern"
        );

        initIcons();
    }
}
