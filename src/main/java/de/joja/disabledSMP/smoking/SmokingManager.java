package de.joja.disabledSMP.smoking;

import de.joja.disabledSMP.DisabledSMP;
import de.joja.disabledSMP.advancements.AdvancementManager;
import de.joja.disabledSMP.utils.ItemUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Cigarettes and vapes: craftable, "eaten" several times (the uses left are shown on the item), and every use
 * fills the area with a dense cloud of smoke.
 */
public class SmokingManager implements Listener {

    public enum Type {
        CIGARETTE("cigarette", "Cigarette", 8),
        VAPE("vape", "Vape", 12);

        public final String id;
        public final String enName;
        public final int maxUses;
        /** Item model in the resource pack (assets/dismp/items/<id>.json). */
        public final Key model;

        Type(String id, String enName, int maxUses) {
            this.id = id;
            this.enName = enName;
            this.maxUses = maxUses;
            this.model = Key.key("dismp", id);
        }
    }

    /**
     * What the items are made of. They are identified by their data, not by this, so it only matters for vanilla:
     * pick something that is (nearly) useless in crafting.
     */
    private static final Material BASE_MATERIAL = Material.DISC_FRAGMENT_5;

    /** "Weed" for the recipes. */
    private static final List<Material> WEED = List.of(
            Material.PITCHER_PLANT, Material.TORCHFLOWER, Material.TALL_GRASS, Material.LARGE_FERN);

    /** How long after one it counts as "right after" for the Addict advancement. */
    private static final long ADDICT_WINDOW_MS = 30_000;

    private record LastSmoke(Type type, long time) {}

    private final Map<UUID, LastSmoke> lastSmoke = new HashMap<>();

    private final JavaPlugin plugin;
    private final NamespacedKey typeKey;
    private final NamespacedKey usesKey;

    public SmokingManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.typeKey = new NamespacedKey(plugin, "smoking_type");
        this.usesKey = new NamespacedKey(plugin, "smoking_uses_left");

        registerRecipes();
    }

    // ---------------------------------------------------------------- recipes

    private void registerRecipes() {
        RecipeChoice weed = new RecipeChoice.MaterialChoice(WEED);

        // weed, paper, paper (in one row)
        register(Type.CIGARETTE, "WPP", recipe -> {
            recipe.setIngredient('W', weed);
            recipe.setIngredient('P', Material.PAPER);
        });

        // weed, iron ingot, copper ingot (in one row)
        register(Type.VAPE, "WIC", recipe -> {
            recipe.setIngredient('W', weed);
            recipe.setIngredient('I', Material.IRON_INGOT);
            recipe.setIngredient('C', Material.COPPER_INGOT);
        });
    }

    private void register(Type type, String shape, java.util.function.Consumer<ShapedRecipe> ingredients) {
        NamespacedKey key = recipeKey(type);

        // don't register it twice (e.g. after a reload)
        Bukkit.removeRecipe(key);

        ShapedRecipe recipe = new ShapedRecipe(key, createItem(type, type.maxUses));
        recipe.shape(shape);
        ingredients.accept(recipe);
        recipe.setCategory(CraftingBookCategory.MISC);
        Bukkit.addRecipe(recipe);

        // unlock it in the recipe book for everyone who is already online
        for (Player player : Bukkit.getOnlinePlayers())
            player.discoverRecipe(key);
    }

    private NamespacedKey recipeKey(Type type) {
        return new NamespacedKey(plugin, type.id);
    }

    /** ... and for everyone who joins later. */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        for (Type type : Type.values())
            event.getPlayer().discoverRecipe(recipeKey(type));
    }

    // ---------------------------------------------------------------- item

    public ItemStack createItem(Type type, int usesLeft) {
        ItemStack item = new ItemStack(BASE_MATERIAL);

        ItemUtils.configureItem(
                item, type.model,
                ItemUtils.translatable("item.dismp." + type.id, type.enName, 0xE0E0E0, false),
                List.of(ItemUtils.translatable("item.dismp.uses_left", "%s/%s left", 0xAAAAAA, false,
                        Component.text(usesLeft), Component.text(type.maxUses)))
        );

        item.setData(DataComponentTypes.MAX_STACK_SIZE, 1);

        // can be "eaten" at any hunger level, without feeding you
        item.setData(DataComponentTypes.FOOD, FoodProperties.food()
                .nutrition(0)
                .saturation(0f)
                .canAlwaysEat(true)
                .build());
        item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                .consumeSeconds(1.6f)
                .animation(ItemUseAnimation.DRINK)
                .sound(Key.key("minecraft", "block.campfire.crackle"))
                .hasConsumeParticles(false)
                .build());

        item.editPersistentDataContainer(pdc -> {
            pdc.set(typeKey, PersistentDataType.STRING, type.id);
            pdc.set(usesKey, PersistentDataType.INTEGER, usesLeft);
        });
        return item;
    }

    /** Whether the item is a cigarette or a vape (smoking isn't eating, so e.g. Toothless doesn't apply). */
    public static boolean isSmokable(ItemStack item) {
        return item != null && item.getType() == BASE_MATERIAL
                && item.getPersistentDataContainer().has(new NamespacedKey(DisabledSMP.plugin, "smoking_type"), PersistentDataType.STRING);
    }

    private Type getType(ItemStack item) {
        if (item == null || item.getType() != BASE_MATERIAL)
            return null;

        String id = item.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
        if (id == null)
            return null;

        for (Type type : Type.values())
            if (type.id.equals(id))
                return type;
        return null;
    }

    // ---------------------------------------------------------------- smoking

    @EventHandler
    public void onSmoke(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        Type type = getType(item);
        if (type == null)
            return;

        Player player = event.getPlayer();

        int usesLeft = item.getPersistentDataContainer().getOrDefault(usesKey, PersistentDataType.INTEGER, type.maxUses) - 1;

        // uses left: swap in the same item with one use less. Last use: let it be consumed as usual.
        if (usesLeft > 0)
            event.setReplacement(createItem(type, usesLeft));

        smokeCloud(player);

        if (item.getPersistentDataContainer().has(typeKey, PersistentDataType.STRING)) {
            String smokeItemType = item.getPersistentDataContainer().get(typeKey, PersistentDataType.STRING);
            System.out.println(smokeItemType);
            if (smokeItemType.equals(Type.VAPE.id))
                player.playSound(
                        player.getLocation(),
                        "dismp:vape",
                        SoundCategory.PLAYERS,
                        1.0f, // Volume
                        1.0f  // Pitch
                );
            else if (smokeItemType.equals(Type.CIGARETTE.id))
                player.playSound(
                        player.getLocation(),
                        "dismp:zigaretten",
                        SoundCategory.PLAYERS,
                        4.5f, // Volume
                        1.0f  // Pitch
                );
            System.out.println("sound");
        }
        System.out.println("after if sound");


        trackAddict(player, type);
    }

    /** Addict: a cigarette and a vape right after each other, in either order. */
    private void trackAddict(Player player, Type type) {
        long now = System.currentTimeMillis();
        LastSmoke last = lastSmoke.get(player.getUniqueId());

        if (last != null && last.type != type && now - last.time <= ADDICT_WINDOW_MS)
            DisabledSMP.plugin.advancementManager.grant(player, AdvancementManager.ADDICT);

        lastSmoke.put(player.getUniqueId(), new LastSmoke(type, now));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastSmoke.remove(event.getPlayer().getUniqueId());
    }

    /** Spread of the cloud: the offsets are standard deviations, so about 2x this is the radius (~3 blocks). */
    private static final double CLOUD_SPREAD_XZ = 1.5;
    private static final double CLOUD_SPREAD_Y = 0.9;

    /** A very dense cloud of campfire smoke around the player (about 3 blocks), that keeps pouring for 3 seconds. */
    private void smokeCloud(Player player) {
        World world = player.getWorld();

        // burst at the mouth
        world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, mouth(player), 250,
                CLOUD_SPREAD_XZ, CLOUD_SPREAD_Y, CLOUD_SPREAD_XZ, 0.01);

        // ... that keeps on pouring and hangs around
        int[] ticks = {0};
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!player.isOnline() || ++ticks[0] > 30) {
                task.cancel();
                return;
            }

            world.spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, player.getEyeLocation(), 30,
                    CLOUD_SPREAD_XZ, CLOUD_SPREAD_Y, CLOUD_SPREAD_XZ, 0.01);
        }, 2L, 2L);
    }

    private static Location mouth(Player player) {
        return player.getEyeLocation()
                .add(player.getLocation().getDirection().multiply(0.4))
                .subtract(0, 0.1, 0);
    }
}
