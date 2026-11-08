package de.joja.disabledSMP.disablities;

import de.joja.disabledSMP.disablities.handlers.*;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import de.joja.disabledSMP.dismenu.menus.Menu;
import net.kyori.adventure.key.Key;

import java.util.List;

public enum Disability {

    ADHD(new ADHD()),
    ALLERGY(new Allergy()),
    BALANCE_DISORDER(new BalanceDisorder()),
    COAGULATION_DISORDER(new CoagulationDisorder()),
    COLOR_BLINDNESS(new ColorBlindness()),
    DWARFISM(new Dwarfism()),
    HEART_ATTACKS(new HeartAttacks()),
    MENTALLY_DISABLED(new MentallyDisabled()),
    NEARSIGHTED(new Nearsighted()),
    OVERWEIGHT(new Overweight()),
    PANIC_ATTACKS(new PanicAttacks()),
    PARALYZED_ARM(new ParalyzedArm()),
    PARALYZED_LEG(new ParalyzedLeg()),
    RACISM(new Racism()),
    SCHIZOPHRENIA(new Schizophrenia()),
    SHORT_TERM_MEMORY(new ShortTermMemory()),
    TOOTHLESS(new Toothless()),
    // keep new ones at the end: the ordinal is saved in the player data and on cure items
    ASTHMA(new Asthma());



    public final DisabilityHandler handler;

    public final String enName;
    public final String enDataName;
    public final List<String> enDescription;

    public final String  deName;
    public final String deDataName;
    public final List<String> deDescription;

    public final Key icon;
    public final Key grayIcon;
    public final Key cureIcon;
    public final Key grayCureIcon;

    public final Menu specificInfoMenu;

    Disability(DisabilityHandler handler) {
        this.handler = handler;

        this.enName = handler.enName;
        this.enDataName = handler.enDataName;
        this.enDescription = handler.enDescription;

        this.deName = handler.deName;
        this.deDataName = handler.deDataName;
        this.deDescription = handler.deDescription;

        this.icon = handler.icon;
        this.grayIcon = handler.grayIcon;
        this.cureIcon = handler.cureIcon;
        this.grayCureIcon = handler.grayCureIcon;

        this.specificInfoMenu = handler.specifcInfoMenu;
    }

    public static final int DISABILITIES_TOTAL_AMOUNT = values().length;

    public static Disability get(int i) {
        return values()[i];
    }
}


