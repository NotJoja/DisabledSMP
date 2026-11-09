package de.joja.disabledSMP.dismenu;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.ColorBlindness;
import de.joja.disabledSMP.disablities.handlers.ColorBlindness.Mode;
import de.joja.disabledSMP.utils.Lang;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static de.joja.disabledSMP.dismenu.DisMenus.button;
import static de.joja.disabledSMP.dismenu.DisMenus.text;

/** /dis menu -> Color Blindness: choose between the two looks of the colorblind pack. */
public final class ColorblindMenu {

    private static final int GOLD = 0xFFAA00;

    private ColorblindMenu() {}

    public static Dialog build(Player player) {
        Lang lang = Lang.of(player);
        ColorBlindness handler = (ColorBlindness) Disability.COLOR_BLINDNESS.handler;
        Mode current = handler.getMode(player);

        List<DialogBody> body = List.of(DialogBody.plainMessage(text(lang.pick(
                "Choose how the world looks to you while you are colorblind.\nThe resource pack is swapped right away.",
                "Wähle, wie die Welt für dich aussieht, solange du farbenblind bist.\nDas Resource Pack wird sofort getauscht."),
                0xFFFFFF, false), 300));

        List<ActionButton> buttons = new ArrayList<>();
        buttons.add(modeButton(lang, handler, current, Mode.SELECTIVE,
                lang.pick("Normal", "Normal"),
                lang.pick("Ores look like coal, tools and armor like iron,\nand colored blocks like wool and concrete are gray.\nEverything else keeps its colors.",
                        "Erze sehen aus wie Kohle, Werkzeuge und Rüstung wie Eisen,\nund farbige Blöcke wie Wolle und Beton sind grau.\nAlles andere behält seine Farben.")));
        buttons.add(modeButton(lang, handler, current, Mode.CLASSIC,
                lang.pick("Classic (black and white)", "Klassisch (schwarz-weiß)"),
                lang.pick("Everything in the world is black and white.",
                        "Alles in der Welt ist schwarz-weiß.")));

        ActionButton back = button(text(lang.pick("Back", "Zurück"), 0xFFFFFF, false), null, 200,
                p -> DisMenus.openDisability(p, Disability.COLOR_BLINDNESS));

        return DisMenus.dialog(text(lang.pick("Color Blindness Mode", "Farbenblindheits-Modus"), 0xFFFFFF, true), body,
                DialogType.multiAction(buttons).exitAction(back).columns(1).build());
    }

    private static ActionButton modeButton(Lang lang, ColorBlindness handler, Mode current, Mode mode, String name, String description) {
        boolean active = mode == current;
        Component label = text((active ? "✔ " : "") + name, active ? GOLD : 0xFFFFFF, active);
        return button(label, text(description, 0xFFFFFF, false), 200, p -> {
            handler.setMode(p, mode);
            p.showDialog(build(p));
        });
    }
}
