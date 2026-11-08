package de.joja.disabledSMP.smoking;

import de.joja.disabledSMP.DisabledSMP;
import de.joja.disabledSMP.advancements.AdvancementManager;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.listeners.JoinListener;
import de.joja.disabledSMP.utils.DConfig;
import de.joja.disabledSMP.utils.Lang;
import de.joja.disabledSMP.utils.ItemUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
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

import java.util.ArrayList;
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
    private final NamespacedKey progressKey;     // on the player: how close they are to the next disability from smoke
    private final NamespacedKey cooldownKey;     // on the player: when they can get the next disability from smoke

    public SmokingManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.typeKey = new NamespacedKey(plugin, "smoking_type");
        this.usesKey = new NamespacedKey(plugin, "smoking_uses_left");

        registerRecipes();

        this.progressKey = new NamespacedKey(plugin, "smoke_disability_progress");
        this.cooldownKey = new NamespacedKey(plugin, "smoke_disability_cooldown");

        // once a second: everyone standing in smoke may catch a disability
        Bukkit.getScheduler().runTaskTimer(plugin, this::tickSmokeZones, 20L, 20L);
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
        smokeZones.add(new SmokeZone(player.getLocation(), System.currentTimeMillis() + SMOKE_ZONE_MS));

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

    // ---------------------------------------------------------------- disabilities from smoke

    /** Where there is smoke: the cloud is about 3 blocks wide and takes a few seconds to thin out. */
    private record SmokeZone(Location center, long expires) {}

    private static final double SMOKE_ZONE_RADIUS = 3.0;
    private static final long SMOKE_ZONE_MS = 8_000;

    private final List<SmokeZone> smokeZones = new ArrayList<>();

    /**
     * How much of the "smoke progress" (0 to 1, a disability at 1) one second in smoke adds. Without any disabilities
     * it takes "seconds_without_disabilities" seconds in the smoke in total (not in a row). The more disabilities, the
     * slower: it falls off to the "min_factor" of that speed once they have as many as a new player starts with.
     */
    private static double smokeProgressPerSecond(int owned) {
        double base = 1.0 / Math.max(1.0, DConfig.SMOKE_DISABILITY_SECONDS);
        int start = Math.max(1, JoinListener.firstJoinAmount());

        double factor = Math.pow(Math.max(0.0, 1.0 - (double) owned / start), 2);
        return base * Math.max(DConfig.SMOKE_DISABILITY_MIN_FACTOR, factor);
    }

    private void tickSmokeZones() {
        long now = System.currentTimeMillis();
        smokeZones.removeIf(zone -> zone.expires < now);

        if (!DConfig.SMOKE_DISABILITY_ENABLED || smokeZones.isEmpty())
            return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!inSmoke(player))
                continue;

            long cooldownUntil = player.getPersistentDataContainer().getOrDefault(cooldownKey, PersistentDataType.LONG, 0L);
            if (now < cooldownUntil)
                continue;

            List<Disability> owned = DisabledSMP.plugin.disabilityMap.get(player.getUniqueId());
            int count = owned == null ? 0 : owned.size();
            if (DConfig.MAX_DISABILITIES >= 0 && count >= DConfig.MAX_DISABILITIES)
                continue;

            // every second in smoke fills the player's "smoke progress", no matter how long ago the last time was
            double progress = player.getPersistentDataContainer().getOrDefault(progressKey, PersistentDataType.DOUBLE, 0.0)
                    + smokeProgressPerSecond(count);
            if (progress >= 1.0) {
                player.getPersistentDataContainer().remove(progressKey);
                catchDisability(player, now);
            } else {
                player.getPersistentDataContainer().set(progressKey, PersistentDataType.DOUBLE, progress);
            }
        }
    }

    private boolean inSmoke(Player player) {
        Location loc = player.getLocation();
        for (SmokeZone zone : smokeZones)
            if (zone.center.getWorld() == loc.getWorld() && zone.center.distanceSquared(loc) <= SMOKE_ZONE_RADIUS * SMOKE_ZONE_RADIUS)
                return true;
        return false;
    }

    private void catchDisability(Player player, long now) {
        List<Disability> owned = DisabledSMP.plugin.disabilityMap.computeIfAbsent(player.getUniqueId(), id -> new ArrayList<>());

        List<Disability> possible = new ArrayList<>(List.of(Disability.values()));
        possible.removeAll(owned);
        if (possible.isEmpty())
            return;

        Disability dis = possible.get(new java.util.Random().nextInt(possible.size()));
        owned.add(dis);
        dis.handler.addToPlayer(player);

        long cooldownMs = DConfig.SMOKE_DISABILITY_COOLDOWN_S * 1000L;
        player.getPersistentDataContainer().set(cooldownKey, PersistentDataType.LONG, now + cooldownMs);

        DConfig.debug(player.getName() + " got " + dis.enName + " from smoke (had " + (owned.size() - 1) + ")");

        Lang lang = Lang.of(player);
        player.sendMessage(Component.text(lang.pick(
                "The smoke got to you... you now have " + dis.enName + "!",
                "Der Rauch hat dich erwischt... du hast jetzt " + dis.deName + "!")));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, 1f, 0.6f);
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
