/*
package de.joja.disabledSMP.disablities;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import java.util.List;

public enum Disability {

    NEARSIGHTED(
            "Nearsighted",
            "You have a reduced view distance",
            "Kurzsichtigkeit",
            "Du hast eine verringerte Sichtweite"
    ),

    PARALYZED_ARM(
            "Paralyzed Arm",
            "You can't use your off-hand and half of your hotbar",
            "Gelähmter Arm",
            "Du kannst nicht deine Off-Hand benutzen&" +
                        "und die Hälfte deiner Hotbar"
    ),

    DWARFISM(
            "Dwarfism",
            "You are smaller and have a reduced reach and max health",
            "Kleinwüchsigkeit",
            "Du bist kleiner und hast eine geringere&" +
                        "Reichweite und weniger maximales Leben"
    ),

    HEART_ATTACKS(
            "Heart Attacks",
            "You may randomly suffer from sudden damage, up to 8 Hearts",
            "Herzinfarkte",
            "Du erleidest zufällig Schaden, bis zu 8 Herzen"
    ),

    PARALYZED_LEG(
            "Paralyzed Leg",
            "You move way slower, but you can craft a wheelchair&" +
                        "to go nearly as fast as normal people",
            "Gelähmtes Bein",
            "Du bewegst dich viel langsamer, aber du&" +
                        "kannst einen Rollstuhl craften um fast&" +
                        "so schnell wie normale Spieler zu sein"
    ),

    SHORT_TERM_MEMORY(
            "Short-Term Memory",
            "Items in your inventory may temporary&" +
                        "change texture, name or position",
            "Kurzzeitgedächtnis",
            "Items in deinem Inventar können temporär&" +
                        "Aussehen, Namen oder Position ändern"
    ),

    COLOR_BLINDNESS(
            "Color Blindness",
            "Certain blocks or items, like ores or&" +
                        "wool, appear indistinguishable to you",
            "Farbenblindheit",
            "Bestimmte Blöcke oder Items, wie Erze oder&" +
                        "Wolle, sehen komplett gleich aus für dich"
    ),

    BALANCE_DISORDER(
            "Balance Disorder",
            "You may randomly get pushed off the edges&" +
                        "of blocks, if you are too close",
            "Gleichgewichtsstörung",
            "Wenn du zu nah an Kanten von Blöcken&" +
                        "bist, wirst du zufällig runtergeschubst"
    ),

    SEIZURES(
            "Seizures",
            "Your crosshair occasionally shakes uncontrollably",
            "Zitteranfälle",
            "Dein Crosshair zittert extrem manchmal"
    ),

    OVERWEIGHT(
            "Overweight",
            "You consume more hunger, move slower and jump lower,&" +
                        "but if you jump from high enough, you can cause an&" +
                        "explosion, that only hurts others",
            "Übergewicht",
            "Du verbrauchst mehr Hunger, bist langsamer und&" +
                        "springst niedriger, aber wenn du von hoch genug&" +
                        "springst, kannst du eine Explosion erzeugen,&" +
                        "die nur Anderen Schaden hinzufügt"
    ),

    TOOTHLESS(
            "Toothless",
            "You can't eat most food, you can only eat soup&" +
                        "normally and you can eat soft food, like apples&" +
                        "or bread with a bit of damage",
            "Zahnlos",
            "Du kannst großteil des Essens nicht essen, nur&" +
                        "Suppen kannst du normal essen, weicheres Essen,&" +
                        "wie Äpfel oder Brot kannst du essen, aber&" +
                        "es schadet dir ein wenig"
    ),

    ALLERGY(
            "Allergy",
            "A random certain food will harm and&" +
                        "weaken you a lot if close to you",
            "Allergie",
            "Ein bestimmtes zufälliges Essen wird dir viel Schaden&" +
                        "hinzufügen und dich schwächen wenn in deiner Nähe"
    ),

    SCHIZOPHRENIA(
            "Schizophrenia",
            "You experience hallucinations, like&" +
                        "fake monsters, blocks or sounds",
            "Schizophrenie",
            "Du erlebst Halluzinationen, wie&" +
                        "fake Mobs, Blöcke oder Sounds"
    ),

    RACISM(
            "Racism",
            "Touching black blocks or items hurts you",
            "Rassismus",
            "Schwarze Blöcke oder Items zu berühren schadet dir"
    );

    public final String enName;
    public final String enDataName;
    public final List<String> enDescription;

    public final String  deName;
    public final String deDataName;
    public final List<String> deDescription;

    Disability(
            String enName,
            String enDescription,
            String deName,
            String deDescription
    ) {
        this.enName = enName;
        this.enDataName = enName.toLowerCase().replace(' ', '_').replace('-', '_');
        this.enDescription = enDescription;
        this.deName = deName;
        this.deDataName = deName.toLowerCase().replace(' ', '_').replace('-', '_');
        this.deDescription = deDescription;
    }

    public static final int DISABILITIES_TOTAL_AMOUNT = Disability.values().length;

    public static Disability get(int i) {
        return values()[i];
    }
}
*/

package de.joja.disabledSMP.disablities;

import de.joja.disabledSMP.disablities.handlers.*;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import de.joja.disabledSMP.dismenu.menus.Menu;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;

import java.util.List;

public enum Disability {

    ADHD(new ADHD()),
    ALLERGY(new Allergy()),
    BALANCE_DISORDER(new BalanceDisorder()),
    COAGULATION_DISORDER(new CoagulationDisorder()),
    COLOR_BLINDNESS(new ColorBlindness()),
    DWARFISM(new Dwarfism()),
    HEART_ATTACKS(new HeartAttacks()),
    NEARSIGHTED(new Nearsighted()),
    OVERWEIGHT(new Overweight()),
    PANIC_ATTACKS(new PanicAttacks()),
    PARALYZED_ARM(new ParalyzedArm()),
    PARALYZED_LEG(new ParalyzedLeg()),
    RACISM(new Racism()),
    SCHIZOPHRENIA(new Schizophrenia()),
    SEIZURES(new Seizures()),
    SHORT_TERM_MEMORY(new ShortTermMemory()),
    TOOTHLESS(new Toothless());



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
    public final Menu specificInfoMenu;

    public final Material[] ritual;

    Disability(DisabilityHandler handler) {
        this.handler = handler;

        this.enName = handler.enName;
        this.enDataName = handler.enDataName;
        this.enDescription = handler.enDescription;

        this.deName = handler.deName;
        this.deDataName = handler.enDataName;
        this.deDescription = handler.deDescription;

        this.icon = handler.icon;
        this.grayIcon = handler.grayIcon;
        this.specificInfoMenu = handler.specifcInfoMenu;
        this.cureIcon = handler.cureIcon;

        this.ritual = handler.ritual;
    }

    public static final int DISABILITIES_TOTAL_AMOUNT = values().length;

    public static Disability get(int i) {
        return values()[i];
    }
}


