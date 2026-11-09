package de.joja.disabledSMP.dismenu;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.Allergy;
import de.joja.disabledSMP.utils.Lang;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static de.joja.disabledSMP.dismenu.DisMenus.button;
import static de.joja.disabledSMP.dismenu.DisMenus.text;

/**
 * The extra info page of the allergy: everything a player can be allergic to. It is the same for everyone, so it
 * doesn't give away what the player is allergic to (and it shows for players without an allergy too).
 */
public final class AllergyMenu {

    private AllergyMenu() {}

    public static Dialog build(Player player) {
        Lang lang = Lang.of(player);

        List<DialogBody> body = new ArrayList<>();
        body.add(DialogBody.plainMessage(text(lang.pick(
                "You can be allergic to one of these.\nWhich one it is, you have to find out yourself.",
                "Du kannst gegen eines davon allergisch sein.\nWogegen, musst du selbst herausfinden."), 0xFFFFFF, false), 300));

        for (Material[] group : Allergy.ALLERGY_TYPES) {
            // the names come as translation keys, so everyone sees them in their client language
            Component names = Component.empty();
            for (int i = 0; i < group.length; i++) {
                if (i > 0)
                    names = names.append(text(", ", 0xAAAAAA, false));
                names = names.append(Component.translatable(group[i].translationKey()).color(net.kyori.adventure.text.format.NamedTextColor.WHITE));
            }
            body.add(DialogBody.item(new ItemStack(group[0]))
                    .description(DialogBody.plainMessage(names, 220))
                    .showTooltip(false)
                    .width(16).height(16)
                    .build());
        }

        return DisMenus.dialog(text(lang.pick("Allergies", "Allergien"), 0xFFFFFF, true), body,
                DialogType.notice(button(text(lang.pick("Back", "Zurück"), 0xFFFFFF, false), null, 200,
                        p -> DisMenus.openDisability(p, Disability.ALLERGY))));
    }
}
