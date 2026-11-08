package de.joja.disabledSMP.dismenu;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.utils.DConfig;
import de.joja.disabledSMP.utils.Lang;
import de.joja.disabledSMP.wheelchair.NailsManager;
import de.joja.disabledSMP.wheelchair.WheelchairManager;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.object.ObjectContents;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.joja.disabledSMP.dismenu.DisMenus.button;
import static de.joja.disabledSMP.dismenu.DisMenus.show;
import static de.joja.disabledSMP.dismenu.DisMenus.text;

/**
 * /dis info: one dialog page per topic. The tab row at the bottom jumps between topics (like paging through
 * origins), every page has its big item icon on top, the text, and, where it applies, the crafting recipes.
 */
public final class InfoMenu {

    private static final int TAB_COLUMNS = 4;
    private static final int TAB_WIDTH = 110;
    private static final int CLOSE_WIDTH = 200;
    private static final int TEXT_WIDTH = 230;
    private static final int RECIPE_WIDTH = 320;

    private static final int WHITE = 0xFFFFFF;
    private static final int HIGHLIGHT = 0xFFC857;
    private static final int MUTED = 0xA8A4A6;
    private static final int TAB_ACTIVE = 0xFFC857;

    private static final Key ITEMS = Key.key("minecraft", "items");
    private static final Key BLOCKS = Key.key("minecraft", "blocks");

    private InfoMenu() {}

    // ------------------------------------------------------------ data

    /** One ingredient: how it is drawn in the recipe grid and what it is called. */
    private record Ingredient(Key atlas, Key sprite, Component name) {}

    /** A shaped recipe; ' ' in a row is an empty slot. */
    private record Recipe(String enTitle, String deTitle, List<String> rows, Map<Character, Ingredient> ingredients,
                          Key resultModel, int amount, String enResult, String deResult) {}

    private record Page(Key tabAtlas, Key tabSprite, Key model,
                        String enTitle, String deTitle,
                        String enText, String deText,
                        List<Recipe> recipes) {}

    private static Ingredient vanilla(Key atlas, String sprite, Material material) {
        return new Ingredient(atlas, Key.key("minecraft", sprite), Component.translatable(material.translationKey()));
    }

    private static final Ingredient PAPER = vanilla(ITEMS, "item/paper", Material.PAPER);
    private static final Ingredient IRON_INGOT = vanilla(ITEMS, "item/iron_ingot", Material.IRON_INGOT);
    private static final Ingredient COPPER_INGOT = vanilla(ITEMS, "item/copper_ingot", Material.COPPER_INGOT);
    private static final Ingredient LEATHER = vanilla(ITEMS, "item/leather", Material.LEATHER);
    private static final Ingredient IRON_BARS = vanilla(BLOCKS, "block/iron_bars", Material.IRON_BARS);
    private static final Ingredient IRON_BLOCK = vanilla(BLOCKS, "block/iron_block", Material.IRON_BLOCK);
    private static final Ingredient RED_WOOL = vanilla(BLOCKS, "block/red_wool", Material.RED_WOOL);
    /** A flat stairs icon drawn for the resource pack (textures/item/icons/oak_stairs.png): vanilla has none. */
    private static final Ingredient OAK_STAIRS = new Ingredient(ITEMS, Key.key("dismp", "item/icons/oak_stairs"),
            Component.translatable(Material.OAK_STAIRS.translationKey()));
    private static final Ingredient WEED = new Ingredient(BLOCKS, Key.key("minecraft", "block/torchflower"),
            Component.text("Weed"));

    private static List<Page> pages() {
        Key wheelchairTab = Key.key("minecraft", "item/leather");
        Key nailsTab = Key.key("minecraft", "item/iron_ingot");
        Key cureModel = Disability.values().length > 0 ? Disability.get(0).cureIcon : Key.key("dismp", "icons/cure");

        Recipe wheelchair = new Recipe("Crafting recipe", "Rezept",
                List.of(" L ", "#O#", "I I"),
                ingredients('L', LEATHER, '#', IRON_BARS, 'O', OAK_STAIRS, 'I', IRON_BLOCK),
                WheelchairManager.WHEELCHAIR_MODEL, 1, "Wheelchair", "Rollstuhl");

        Recipe cigarette = new Recipe("Cigarette", "Zigarette",
                List.of("WPP"),
                ingredients('W', WEED, 'P', PAPER),
                Key.key("dismp", "cigarette"), 1, "Cigarette", "Zigarette");

        Recipe vape = new Recipe("Vape", "Vape",
                List.of("WIC"),
                ingredients('W', WEED, 'I', IRON_INGOT, 'C', COPPER_INGOT),
                Key.key("dismp", "vape"), 1, "Vape", "Vape");

        Recipe nails = new Recipe("Crafting recipe", "Rezept",
                List.of("III"),
                ingredients('I', IRON_INGOT),
                NailsManager.NAILS_MODEL, 3, "Nails", "Nägel");

        Recipe bandage = new Recipe("Crafting recipe", "Rezept",
                List.of(" P ", "PRP", " P "),
                ingredients('P', PAPER, 'R', RED_WOOL),
                Key.key("dismp", "bandage"), 1, "Bandage", "Bandage");

        int startAmount = DConfig.FIRST_JOIN_DISABILITIES >= 0
                ? DConfig.FIRST_JOIN_DISABILITIES : Disability.DISABILITIES_TOTAL_AMOUNT / 2;
        int total = Disability.DISABILITIES_TOTAL_AMOUNT;
        boolean half = DConfig.FIRST_JOIN_DISABILITIES < 0;

        return List.of(
                new Page(ITEMS, Key.key("dismp", "item/pack_icon"), Key.key("dismp", "pack_icon"),
                        "DisabledSMP", "DisabledSMP",
                        "You start with **" + startAmount + " random disabilities**" + (half ? " (half of all of them)" : "") + ".\n\n"
                                + "When you **kill a player**, you pass one of your disabilities on to them.\n"
                                + "When you **get killed**, you receive one of theirs.\n\n"
                                + "Use the tabs below to learn about everything else.",
                        "Du startest mit **" + startAmount + " zufälligen Disabilities**" + (half ? " (der Hälfte aller)" : "") + ".\n\n"
                                + "Wenn du **einen Spieler tötest**, gibst du ihm eine deiner Disabilities weiter.\n"
                                + "Wenn du **getötet wirst**, bekommst du eine von seinen.\n\n"
                                + "Mit den Reitern unten erfährst du alles Weitere.",
                        List.of()),

                new Page(ITEMS, Key.key("dismp", "item/icons/info"), Key.key("dismp", "icons/info"),
                        "Disabilities", "Disabilities",
                        "There are **" + total + " different disabilities**, and each one changes the game in its own way.\n\n"
                                + "Use **/dis menu** to read about the disabilities you have and to unlock your first custom advancement.",
                        "Es gibt **" + total + " verschiedene Disabilities** und jede verändert das Spiel auf ihre eigene Art.\n\n"
                                + "Mit **/dis menu** erfährst du mehr über deine Disabilities und schaltest dein erstes eigenes Advancement frei.",
                        List.of()),

                new Page(ITEMS, Key.key("dismp", "item/" + cureModel.value()), cureModel,
                        "Cures", "Heilmittel",
                        "A **cure** is an item that removes its disability when you **eat** it.\n\n"
                                + "To get one, **withdraw** it in the **/dis menu**. This only works for a disability you **don't** have: "
                                + "you take that disability on and receive its cure.\n\n"
                                + "Cures can't explode or burn.",
                        "Ein **Heilmittel** ist ein Item, das seine Disability entfernt, wenn du es **isst**.\n\n"
                                + "Du bekommst eins, indem du es im **/dis menu** **extrahierst**. Das geht nur bei einer Disability, die du **nicht** hast: "
                                + "du nimmst diese Disability auf dich und erhältst dafür ihr Heilmittel.\n\n"
                                + "Heilmittel können weder explodieren noch verbrennen.",
                        List.of()),

                new Page(ITEMS, wheelchairTab, WheelchairManager.WHEELCHAIR_MODEL,
                        "Wheelchair", "Rollstuhl",
                        "A wheelchair lets you drive around at **normal player speed**, no matter which disabilities you have.\n\n"
                                + "Press **space** to give it a small boost.\n\n"
                                + "Players with **overweight** will break wheelchairs.",
                        "Mit einem Rollstuhl fährst du in **normaler Spielergeschwindigkeit**, egal welche Disabilities du hast.\n\n"
                                + "Drücke **Leertaste** für einen kleinen Schub.\n\n"
                                + "Spieler mit **Übergewicht** machen Rollstühle kaputt.",
                        List.of(wheelchair)),

                new Page(ITEMS, Key.key("dismp", "item/vape"), Key.key("dismp", "vape"),
                        "Smoking", "Rauchen",
                        "Cigarettes and vapes are crafted from any kind of **weed**: torchflowers, pitcher plants, tall grass and large ferns.\n\n"
                                + "A **vape has 12 uses**, a **cigarette only 8**. Using one creates **smoke**.\n\n"
                                + "Standing in smoke can give you a **new disability**. The fewer disabilities you already have, the faster it happens.",
                        "Zigaretten und Vapes werden aus beliebigem **Kraut** gecraftet: Fackelblumen, Krugpflanzen, hohes Gras und große Farne.\n\n"
                                + "Eine **Vape hat 12 Züge**, eine **Zigarette nur 8**. Beim Benutzen entsteht **Rauch**.\n\n"
                                + "Wer im Rauch steht, kann eine **neue Disability** bekommen. Je weniger Disabilities man schon hat, desto schneller geht das.",
                        List.of(cigarette, vape)),

                new Page(ITEMS, nailsTab, NailsManager.NAILS_MODEL,
                        "Nails", "Nägel",
                        "Nails are placed on the ground.\n\n"
                                + "They **damage** everything that stands on them and **slow down** wheelchairs.",
                        "Nägel werden auf den Boden gesetzt.\n\n"
                                + "Sie **verletzen** alles, was darauf steht, und **bremsen** Rollstühle.",
                        List.of(nails)),

                new Page(ITEMS, Key.key("dismp", "item/bandage"), Key.key("dismp", "bandage"),
                        "Bandages", "Bandagen",
                        "Players with **coagulation disorder** can't regenerate health on their own.\n\n"
                                + "Using a **bandage** lets them heal for a short while.",
                        "Spieler mit **Gerinnungsstörung** können sich nicht von selbst regenerieren.\n\n"
                                + "Mit einer **Bandage** können sie sich kurze Zeit lang heilen.",
                        List.of(bandage)));
    }

    private static Map<Character, Ingredient> ingredients(Object... pairs) {
        Map<Character, Ingredient> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2)
            map.put((Character) pairs[i], (Ingredient) pairs[i + 1]);
        return map;
    }

    // ------------------------------------------------------------ opening

    public static void open(Player player) {
        open(player, 0);
    }

    public static void open(Player player, int index) {
        Lang lang = Lang.of(player);
        List<Page> pages = pages();
        Page page = pages.get(Math.max(0, Math.min(index, pages.size() - 1)));

        // title: icon + name
        Component title = sprite(page.tabAtlas(), page.tabSprite())
                .append(text(" " + lang.pick(page.enTitle(), page.deTitle()), HIGHLIGHT, true));

        // big item icon next to the text
        ItemStack icon = new ItemStack(Material.IRON_NUGGET);
        icon.setData(DataComponentTypes.ITEM_MODEL, page.model());

        List<DialogBody> body = new ArrayList<>();
        body.add(DialogBody.item(icon)
                .description(DialogBody.plainMessage(rich(lang.pick(page.enText(), page.deText())), TEXT_WIDTH))
                .showDecorations(false)
                .showTooltip(false)
                .width(48).height(48)
                .build());

        for (Recipe recipe : page.recipes()) {
            body.add(DialogBody.plainMessage(recipeComponent(lang, recipe), RECIPE_WIDTH));

            // the result is shown as the real item (its model), not a flat texture
            ItemStack result = new ItemStack(Material.IRON_NUGGET);
            result.setData(DataComponentTypes.ITEM_MODEL, recipe.resultModel());
            Component resultName = text((recipe.amount() > 1 ? recipe.amount() + "× " : "")
                    + lang.pick(recipe.enResult(), recipe.deResult()), WHITE, true);
            body.add(DialogBody.item(result)
                    .description(DialogBody.plainMessage(
                            text("→ ", MUTED, true).append(resultName), 150))
                    .showDecorations(false)
                    .showTooltip(false)
                    .width(32).height(32)
                    .build());
        }

        // tabs
        List<ActionButton> tabs = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            Page tab = pages.get(i);
            boolean active = i == index;
            Component label = sprite(tab.tabAtlas(), tab.tabSprite())
                    .append(text(" " + lang.pick(tab.enTitle(), tab.deTitle()), active ? TAB_ACTIVE : WHITE, active));
            int target = i;
            tabs.add(button(label, null, TAB_WIDTH, p -> open(p, target)));
        }

        ActionButton close = button(text(lang.pick("Close", "Schließen"), WHITE, false), null, CLOSE_WIDTH,
                Player::closeDialog);

        show(player, title, body,
                DialogType.multiAction(tabs).exitAction(close).columns(TAB_COLUMNS).build());
    }

    // ------------------------------------------------------------ rendering

    private static Component sprite(Key atlas, Key sprite) {
        // explicit white / not bold, so a sprite is never tinted by the colour of whatever it is appended to
        return Component.object(ObjectContents.sprite(atlas, sprite))
                .color(net.kyori.adventure.text.format.TextColor.color(WHITE))
                .decoration(TextDecoration.BOLD, false);
    }

    private static Component ingredientSprite(Ingredient ingredient) {
        return sprite(ingredient.atlas(), ingredient.sprite());
    }

    /** The recipe as a little grid of sprites, an arrow and the result, followed by a legend with the names. */
    private static Component recipeComponent(Lang lang, Recipe recipe) {
        // start from an empty parent: children inherit the parent's style (that tinted the sprites yellow)
        Component out = Component.empty().append(text(lang.pick(recipe.enTitle(), recipe.deTitle()), HIGHLIGHT, true));

        for (int r = 0; r < recipe.rows().size(); r++) {
            String row = recipe.rows().get(r);
            Component line = Component.empty();
            for (int c = 0; c < row.length(); c++) {
                Ingredient ingredient = recipe.ingredients().get(row.charAt(c));
                line = line.append(ingredient == null ? text("   ", WHITE, false) : ingredientSprite(ingredient));
                if (c < row.length() - 1)
                    line = line.append(text(" ", WHITE, false));
            }
            out = out.appendNewline().append(line);
        }

        return out;
    }

    /** Text with **bold highlighted** parts. */
    private static Component rich(String markup) {
        Component out = Component.empty();
        String[] parts = markup.split("\\*\\*", -1);
        for (int i = 0; i < parts.length; i++)
            if (!parts[i].isEmpty())
                out = out.append(i % 2 == 1 ? text(parts[i], HIGHLIGHT, true) : text(parts[i], WHITE, false));
        return out;
    }

}
