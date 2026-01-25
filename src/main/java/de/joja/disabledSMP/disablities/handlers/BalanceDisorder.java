package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import java.util.List;

public class BalanceDisorder extends DisabilityHandler {

    public BalanceDisorder() {
        this.enName = "Balance Disorder";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You may randomly get pushed",
                "off block edges"
        );

        this.deName = "Gleichgewichtsstörung";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du wirst zufällig von",
                "Blockkanten geschubst"
        );
    }
}
