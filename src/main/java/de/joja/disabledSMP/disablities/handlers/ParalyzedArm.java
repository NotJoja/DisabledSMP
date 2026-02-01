package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import java.util.List;

public class ParalyzedArm extends DisabilityHandler {

    public ParalyzedArm() {
        this.enName = "Paralyzed Arm";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You can't use your off-hand and half of your hotbar"
        );

        this.deName = "Gelähmter Arm";
        this.deDataName = toDataName(enName);
        this.deDescription = List.of(
                "Du kannst nicht deine Off-Hand benutzen",
                "und die Hälfte deiner Hotbar"
        );

        initIcons();
    }
}
