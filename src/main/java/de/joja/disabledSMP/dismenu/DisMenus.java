package de.joja.disabledSMP.dismenu;

import de.joja.disabledSMP.disablities.CureManager;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.utils.Lang;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.utils.Icons.*;

/**
 * The /dis menu, built from Minecraft's native dialog screens. Navigation keeps the screen open
 * (after-action NONE), so going between pages just swaps the dialog instead of closing and reopening it.
 */
public final class DisMenus {

    private static final int GRID_COLUMNS = 3;
    private static final int GRID_BUTTON_WIDTH = 130;
    private static final int WIDE_BUTTON_WIDTH = 200;
    private static final int BODY_WIDTH = 300;

    private static final ClickCallback.Options CLICK_OPTIONS = ClickCallback.Options.builder()
            .uses(ClickCallback.UNLIMITED_USES)
            .build();

    private DisMenus() {}

    // ------------------------------------------------------------ main page

    public static void openMain(Player player) {
        Lang lang = Lang.of(player);
        List<Disability> owned = plugin.disabilityMap.get(player.getUniqueId());
        int ownedCount = owned == null ? 0 : (int) owned.stream().filter(Disability::isEnabled).count();

        Component summary = text(lang.pick(
                "You have " + ownedCount + " of " + Disability.enabled().size() + " disabilities.\nPick one to see more.",
                "Du hast " + ownedCount + " von " + Disability.enabled().size() + " Disabilities.\nWähle eine aus, um mehr zu sehen."),
                0xFFFFFF, false);

        List<ActionButton> buttons = new ArrayList<>();
        for (Disability dis : Disability.enabled()) {
            boolean has = owned != null && owned.contains(dis);
            Component tooltip = description(lang, dis)
                    .appendNewline().appendNewline()
                    .append(text(has
                            ? lang.pick("You have this disability", "Du hast diese Disability")
                            : lang.pick("You don't have this disability", "Du hast diese Disability nicht"),
                            has ? DIS_RED_RGB : DIS_GRAY_RGB, false));
            buttons.add(button(iconLabel(has ? dis.icon : dis.grayIcon, lang.pick(dis.enName, dis.deName), has ? DIS_RED_RGB : DIS_GRAY_RGB, true),
                    tooltip, GRID_BUTTON_WIDTH, p -> openDisability(p, dis)));
        }

        ActionButton close = button(text(lang.pick("Close", "Schließen"), 0xFFFFFF, false), null, WIDE_BUTTON_WIDTH,
                Player::closeDialog);

        show(player, text("Disabled Menu", 0xFFFFFF, true),
                List.of(DialogBody.plainMessage(summary, BODY_WIDTH)),
                DialogType.multiAction(buttons).exitAction(close).columns(GRID_COLUMNS).build());
    }

    // ------------------------------------------------------------ single disability page

    public static void openDisability(Player player, Disability dis) {
        Lang lang = Lang.of(player);
        List<Disability> owned = plugin.disabilityMap.get(player.getUniqueId());
        boolean has = owned != null && owned.contains(dis);
        int rgb = has ? DIS_RED_RGB : DIS_GRAY_RGB;

        ItemStack iconItem = new ItemStack(Material.IRON_NUGGET);
        iconItem.setData(DataComponentTypes.ITEM_MODEL, has ? dis.icon : dis.grayIcon);

        Component status = text(has
                ? lang.pick("You have this disability.", "Du hast diese Disability.")
                : lang.pick("You don't have this disability.", "Du hast diese Disability nicht."),
                rgb, true);

        List<DialogBody> body = List.of(
                DialogBody.item(iconItem)
                        .description(DialogBody.plainMessage(description(lang, dis).appendNewline().appendNewline().append(status), 220))
                        .showTooltip(false)
                        .width(32).height(32)
                        .build());

        List<ActionButton> buttons = new ArrayList<>();

        // withdrawing a cure only works while you don't have the disability
        if (has) {
            buttons.add(button(iconLabel(dis.grayCureIcon, lang.pick("Withdraw Cure", "Heilmittel Extrahieren"), DIS_GRAY_RGB, false),
                    text(lang.pick("Only possible if you don't have that Disability!",
                            "Nur möglich wenn du diese Disability nicht hast!"), 0xFFFFFF, false),
                    WIDE_BUTTON_WIDTH, p -> {}));
        } else {
            buttons.add(button(iconLabel(dis.cureIcon, lang.pick("Withdraw Cure", "Heilmittel Extrahieren"), CURE_RGB, false),
                    text(lang.pick("Take this disability and get its cure item.",
                            "Nimm diese Disability auf dich und erhalte das Heilmittel."), 0xFFFFFF, false),
                    WIDE_BUTTON_WIDTH, p -> withdrawCure(p, dis)));
        }

        if (dis.specificInfoDialog != null)
            buttons.add(button(text(lang.pick("Extra Disability Info", "Extra Disability Info"), 0xF0F0FF, false), null,
                    WIDE_BUTTON_WIDTH, p -> p.showDialog(dis.specificInfoDialog.apply(p))));

        ActionButton back = button(text(lang.pick("Back", "Zurück"), 0xFFFFFF, false), null, WIDE_BUTTON_WIDTH,
                DisMenus::openMain);

        show(player, text(lang.pick(dis.enName, dis.deName), rgb, true), body,
                DialogType.multiAction(buttons).exitAction(back).columns(1).build());
    }

    private static void withdrawCure(Player player, Disability dis) {
        List<Disability> playerDisabilities = plugin.disabilityMap.get(player.getUniqueId());
        if (playerDisabilities == null || playerDisabilities.contains(dis))
            return;
        player.give(CureManager.createCureItem(dis));
        playerDisabilities.add(dis);
        dis.handler.addToPlayer(player);
        player.closeDialog();
    }

    // ------------------------------------------------------------ helpers

    static void show(Player player, Component title, List<DialogBody> body, DialogType type) {
        player.showDialog(dialog(title, body, type));
    }

    static Dialog dialog(Component title, List<DialogBody> body, DialogType type) {
        return Dialog.create(factory -> factory.empty()
                .base(DialogBase.builder(title)
                        .body(body)
                        .canCloseWithEscape(true)
                        .pause(false)
                        .afterAction(DialogBase.DialogAfterAction.NONE)
                        .build())
                .type(type));
    }

    static ActionButton button(Component label, Component tooltip, int width, Consumer<Player> onClick) {
        DialogActionCallback callback = (view, audience) -> {
            if (audience instanceof Player p)
                onClick.accept(p);
        };
        return ActionButton.create(label, tooltip, width, DialogAction.customClick(callback, CLICK_OPTIONS));
    }

    /** "[icon] Name": the icon is the item texture from the resource pack, drawn as an inline sprite. */
    private static Component iconLabel(Key icon, String name, int rgb, boolean bold) {
        Key sprite = Key.key(icon.namespace(), "item/" + icon.value());
        return Component.object(ObjectContents.sprite(Key.key("minecraft", "items"), sprite))
                .append(text(" " + name, rgb, bold));
    }

    private static Component description(Lang lang, Disability dis) {
        List<String> lines = lang.pick(dis.enDescription, dis.deDescription);
        Component result = Component.empty();
        if (lines == null)
            return result;
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0)
                result = result.append(text(" ", 0xFFFFFF, false));
            result = result.append(text(lines.get(i), 0xFFFFFF, false));
        }
        return result;
    }

    static Component text(String text, int rgb, boolean bold) {
        return Component.text(text)
                .color(TextColor.color(rgb))
                .decoration(TextDecoration.BOLD, bold)
                .decoration(TextDecoration.ITALIC, false);
    }

}
