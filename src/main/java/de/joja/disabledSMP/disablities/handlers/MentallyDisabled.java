package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import de.joja.disabledSMP.utils.DConfig;
import de.joja.disabledSMP.utils.Lang;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static de.joja.disabledSMP.utils.Utils.random;

public class MentallyDisabled extends DisabilityHandler {

    public MentallyDisabled() {
        this.enName = "Mentally Disabled";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "Crafting sometimes goes wrong:",
                "you can end up with another item",
                "made from the same materials"
        );

        this.deName = "Geistlich Behindert";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Beim Craften geht manchmal etwas schief:",
                "Du kannst ein anderes Item bekommen,",
                "das aus denselben Materialien besteht"
        );

        initIcons();
    }

    /** Chance that taking the result out of the crafting grid gives a different item. */
    private static final double MISCRAFT_CHANCE = DConfig.MENTALLY_DISABLED_MISCRAFT_CHANCE;

    /** Safety net for shift-clicking (a full 3x3 grid of stackable items is at most 64 crafts). */
    private static final int MAX_SHIFT_CRAFTS = 64 * 9;

    /**
     * The recipe looks normal while crafting. Only when the result is taken out, there is a chance that a different
     * recipe is crafted instead, one that can be made from (a part of) the items in the grid. Only the items that
     * recipe needs are used up, the rest stays in the crafting grid.
     * <p>
     * Shift-clicking crafts as often as possible, the chance is rolled for every single craft and it stops at the
     * first miscraft.
     */
    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (!hasDis(player, Disability.MENTALLY_DISABLED))
            return;

        CraftingInventory inventory = event.getInventory();

        if (event.isShiftClick()) {
            event.setCancelled(true);
            shiftCraft(player, inventory);
            return;
        }

        if (random.nextDouble() >= MISCRAFT_CHANCE)
            return;

        ItemStack[] matrix = inventory.getMatrix();
        Recipe original = event.getRecipe();
        Recipe chosen = pickAlternative(matrix, original.getResult().getType());
        if (chosen == null)
            return;     // nothing else is possible with these items, craft normally

        ItemStack result = chosen.getResult().clone();
        DConfig.debug(player.getName() + " miscrafted " + original.getResult().getType() + " -> " + result.getType());

        // the result goes onto the cursor for a normal click, and straight into the inventory otherwise
        ClickType click = event.getClick();
        boolean toCursor = click == ClickType.LEFT || click == ClickType.RIGHT;

        ItemStack cursor = event.getCursor();
        boolean cursorEmpty = cursor == null || cursor.getType().isAir();
        if (toCursor && !cursorEmpty
                && (!cursor.isSimilar(result) || cursor.getAmount() + result.getAmount() > cursor.getMaxStackSize())) {
            event.setCancelled(true);       // doesn't fit, nothing happens (like in vanilla)
            return;
        }

        event.setCancelled(true);

        List<ItemStack> remainders = consume(chosen, matrix);
        inventory.setMatrix(matrix);

        if (toCursor) {
            if (cursorEmpty) {
                player.setItemOnCursor(result);
            } else {
                cursor.setAmount(cursor.getAmount() + result.getAmount());
                player.setItemOnCursor(cursor);
            }
        } else {
            give(player, result);
        }

        for (ItemStack remainder : remainders)
            give(player, remainder);

        player.updateInventory();
    }

    /** Shift-click: craft over and over, every craft can go wrong, the first miscraft ends it. */
    private void shiftCraft(Player player, CraftingInventory inventory) {
        ItemStack[] matrix = inventory.getMatrix();

        for (int i = 0; i < MAX_SHIFT_CRAFTS; i++) {
            Recipe recipe = Bukkit.getCraftingRecipe(matrix, player.getWorld());
            if (recipe == null)
                break;

            Recipe chosen = random.nextDouble() < MISCRAFT_CHANCE
                    ? pickAlternative(matrix, recipe.getResult().getType())
                    : null;
            boolean miscraft = chosen != null;

            // the result is worked out before anything is used up (on a copy for a normal craft)
            ItemStack result;
            if (miscraft) {
                result = chosen.getResult().clone();
            } else {
                ItemStack[] copy = new ItemStack[matrix.length];
                for (int slot = 0; slot < matrix.length; slot++)
                    copy[slot] = matrix[slot] == null ? null : matrix[slot].clone();

                result = Bukkit.craftItem(copy, player.getWorld(), player);
                if (result.getType().isAir())
                    break;
            }

            // like vanilla: stop when the result doesn't fit into the inventory anymore
            if (!fits(player, result))
                break;

            List<ItemStack> remainders;
            if (miscraft) {
                remainders = consume(chosen, matrix);
            } else {
                remainders = new ArrayList<>();
                for (int slot = 0; slot < matrix.length; slot++)
                    if (matrix[slot] != null && !matrix[slot].getType().isAir())
                        useOne(matrix, slot, remainders);
            }

            give(player, result);
            for (ItemStack remainder : remainders)
                give(player, remainder);

            if (miscraft)
                break;
        }

        inventory.setMatrix(matrix);
        player.updateInventory();
    }

    /** Uses up the ingredients of the recipe only (one item each) and returns what is left over (buckets, ...). */
    private static List<ItemStack> consume(Recipe recipe, ItemStack[] matrix) {
        List<ItemStack> remainders = new ArrayList<>();
        int[] slots = assign(ingredients(recipe), matrix);
        if (slots != null)
            for (int slot : slots)
                useOne(matrix, slot, remainders);
        return remainders;
    }

    private static void useOne(ItemStack[] matrix, int slot, List<ItemStack> remainders) {
        ItemStack item = matrix[slot];
        Material remainder = item.getType().getCraftingRemainingItem();
        if (remainder != null)
            remainders.add(new ItemStack(remainder));

        if (item.getAmount() <= 1)
            matrix[slot] = null;
        else
            item.setAmount(item.getAmount() - 1);
    }

    private static boolean fits(Player player, ItemStack item) {
        int space = 0;
        for (ItemStack slot : player.getInventory().getStorageContents()) {
            if (slot == null || slot.getType().isAir())
                space += item.getMaxStackSize();
            else if (slot.isSimilar(item))
                space += slot.getMaxStackSize() - slot.getAmount();
        }
        return space >= item.getAmount();
    }

    private static void give(Player player, ItemStack item) {
        for (ItemStack left : player.getInventory().addItem(item).values())
            player.getWorld().dropItem(player.getLocation(), left);
    }

    /** A random recipe (other than the original) that can be crafted from the items in the grid, or null. */
    private static Recipe pickAlternative(ItemStack[] matrix, Material originalResult) {
        List<Recipe> alternatives = findAlternatives(matrix, originalResult);
        return alternatives.isEmpty() ? null : alternatives.get(random.nextInt(alternatives.size()));
    }

    /** Every other recipe that can be crafted from (a part of) the items in the grid. */
    private static List<Recipe> findAlternatives(ItemStack[] matrix, Material originalResult) {
        int size = matrix.length == 4 ? 2 : 3;

        List<Recipe> found = new ArrayList<>();
        Iterator<Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            Recipe recipe = it.next();

            if (!(recipe instanceof ShapedRecipe) && !(recipe instanceof ShapelessRecipe))
                continue;

            Material result = recipe.getResult().getType();
            if (result.isAir() || result == originalResult)
                continue;

            // has to fit into the grid
            if (recipe instanceof ShapedRecipe shaped) {
                String[] shape = shaped.getShape();
                int width = 0;
                for (String row : shape)
                    width = Math.max(width, row.length());
                if (width > size || shape.length > size)
                    continue;
            }

            List<RecipeChoice> choices = ingredients(recipe);
            if (choices.isEmpty() || choices.size() > matrix.length)
                continue;

            if (assign(choices, matrix) != null)
                found.add(recipe);
        }
        return found;
    }

    private static List<RecipeChoice> ingredients(Recipe recipe) {
        List<RecipeChoice> choices = new ArrayList<>();

        if (recipe instanceof ShapelessRecipe shapeless) {
            choices.addAll(shapeless.getChoiceList());
        } else if (recipe instanceof ShapedRecipe shaped) {
            Map<Character, RecipeChoice> map = shaped.getChoiceMap();
            for (String row : shaped.getShape())
                for (char c : row.toCharArray()) {
                    RecipeChoice choice = map.get(c);
                    if (choice != null)
                        choices.add(choice);
                }
        }
        return choices;
    }

    /** Gives every ingredient its own slot of the grid (one item each). Returns the slots, or null if impossible. */
    private static int[] assign(List<RecipeChoice> choices, ItemStack[] matrix) {
        int[] slots = new int[choices.size()];
        return assign(choices, matrix, 0, new boolean[matrix.length], slots) ? slots : null;
    }

    private static boolean assign(List<RecipeChoice> choices, ItemStack[] matrix, int index, boolean[] used, int[] slots) {
        if (index == choices.size())
            return true;

        for (int slot = 0; slot < matrix.length; slot++) {
            ItemStack item = matrix[slot];
            if (used[slot] || item == null || item.getType().isAir() || !choices.get(index).test(item))
                continue;

            used[slot] = true;
            slots[index] = slot;
            if (assign(choices, matrix, index + 1, used, slots))
                return true;
            used[slot] = false;
        }
        return false;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {

        Player player = event.getPlayer();
        if (!DConfig.MENTALLY_DISABLED_CHAT_SHUFFLE || !hasDis(player, Disability.MENTALLY_DISABLED))
            return;

        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        List<String> words = new ArrayList<>(List.of(message.trim().split("\\s+")));
        if (words.size() < 2 || words.stream().distinct().count() < 2)
            return;     // nothing to swap around

        // shuffle until the order is actually different
        List<String> original = List.copyOf(words);
        while (words.equals(original))
            java.util.Collections.shuffle(words, random);

        StringBuilder strBuilder = new StringBuilder(String.join(" ", words));

        event.message(Component.text(strBuilder.toString()));
    }

    @EventHandler
    public void onFemboyChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID jojaUUID = UUID.fromString("13ad46862f814ea1bd776ef2d89f0964");
        String jojaName = "Joja_bings";
        if (!(player.getUniqueId().equals(jojaUUID) || player.getName().equals(jojaName)))
            return;

        if (Math.random() > 0.01)
            return;

        event.message(Component.text(Lang.of(player).pick("I'm a horny femboy :3", "Ich bin ein geiler femboy :3")));
    }




}
