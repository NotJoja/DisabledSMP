package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import java.util.List;

public class ADHD extends DisabilityHandler {

    public ADHD() {
        this.enName = "ADHD";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You can't stay for too",
                "long in the same area"
        );

        this.deName = "ADHS";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du kannst nicht zu lange",
                "am selben Ort bleiben"
        );

        initIcons();
    }
}
