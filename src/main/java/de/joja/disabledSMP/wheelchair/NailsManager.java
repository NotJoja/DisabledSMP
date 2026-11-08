package de.joja.disabledSMP.wheelchair;

import de.joja.disabledSMP.DisabledSMP;
import de.joja.disabledSMP.utils.ItemUtils;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.concurrent.ThreadLocalRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import io.papermc.paper.datacomponent.DataComponentTypes;

/**
 * Nails: a flat "block" lying on the ground that hurts everyone who walks over it.
 *
 * Technically an Interaction entity (the hitbox, about the height of 2 snow layers) with an ItemDisplay showing the
 * block model of the resource pack. Break it by hitting it.
 */
public class NailsManager implements Listener {

    public static final Key NAILS_MODEL = Key.key("dismp", "nails");

    // ---------------------------------------------------------------- tuning

    /** Height of the hitbox: 2 snow layers = 2/8 blocks. */
    private static final float HEIGHT = 0.25f;
    private static final float WIDTH = 0.9f;

    /** Damage per hit (2 = one heart), the invulnerability time of vanilla damage limits how often it hits. */
    private static final double DAMAGE = 2.0;

    private static final int CHECK_INTERVAL_TICKS = 10;

    /** What the hits have to add up to: 3 hits with the hand (1 damage), 1 with a sword or an axe (4+). */
    private static final double HEALTH = 3.0;

    /** How many nails one craft gives. */
    private static final int CRAFT_AMOUNT = 3;

    // ---------------------------------------------------------------- keys

    private final JavaPlugin plugin;
    private final NamespacedKey itemKey;      // on the item
    private final NamespacedKey nailsKey;     // on the interaction
    private final NamespacedKey displayKey;   // on the interaction: uuid of its display
    private final NamespacedKey healthKey;   // on the interaction: hits it can take
    private final NamespacedKey recipeKey;

    public NailsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "nails_item");
        this.nailsKey = new NamespacedKey(plugin, "nails");
        this.displayKey = new NamespacedKey(plugin, "nails_display");
        this.healthKey = new NamespacedKey(plugin, "nails_health");
        this.recipeKey = new NamespacedKey(plugin, "nails");

        registerRecipe();

        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, CHECK_INTERVAL_TICKS, CHECK_INTERVAL_TICKS);
    }

    // ---------------------------------------------------------------- recipe / item

    private void registerRecipe() {
        Bukkit.removeRecipe(recipeKey);

        ItemStack result = createItem();
        result.setAmount(CRAFT_AMOUNT);

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, result);
        recipe.shape("III");
        recipe.setIngredient('I', Material.IRON_INGOT);
        Bukkit.addRecipe(recipe);

        for (Player player : Bukkit.getOnlinePlayers())
            player.discoverRecipe(recipeKey);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().discoverRecipe(recipeKey);
    }

    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemUtils.configureItem(
                item, NAILS_MODEL,
                ItemUtils.translatable("item.dismp.nails", "Nails", 0xC8C8C8, false),
                List.of(
                        ItemUtils.translatable("item.dismp.nails.lore.1", "Right-click on the ground to place them.", 0xFFFFFF, false),
                        ItemUtils.translatable("item.dismp.nails.lore.2", "Hurts everyone who steps on them.", 0xFFFFFF, false)
                )
        );
        item.editPersistentDataContainer(pdc -> pdc.set(itemKey, PersistentDataType.BYTE, (byte) 1));
        return item;
    }

    private boolean isNailsItem(ItemStack item) {
        return item != null && item.getType() == Material.PAPER
                && item.getPersistentDataContainer().has(itemKey, PersistentDataType.BYTE);
    }

    private boolean isNails(Entity entity) {
        return entity instanceof Interaction
                && entity.getPersistentDataContainer().has(nailsKey, PersistentDataType.BYTE);
    }

    private ItemDisplay getDisplay(Entity nails) {
        String uuid = nails.getPersistentDataContainer().get(displayKey, PersistentDataType.STRING);
        if (uuid == null)
            return null;
        return Bukkit.getEntity(UUID.fromString(uuid)) instanceof ItemDisplay display ? display : null;
    }

    // ---------------------------------------------------------------- spawning

    private void spawn(Location loc) {
        // lie rotated by a random multiple of 90 degrees, parallel to the blocks
        float yaw = 90f * ThreadLocalRandom.current().nextInt(4);
        Block block = loc.getBlock();
        Display.Brightness brightness = new Display.Brightness(block.getLightFromBlocks(), block.getLightFromSky());

        Interaction hitbox = loc.getWorld().spawn(loc, Interaction.class, i -> {
            i.setInteractionWidth(WIDTH);
            i.setInteractionHeight(HEIGHT);
            i.setResponsive(true);
            i.setPersistent(true);
            i.getPersistentDataContainer().set(nailsKey, PersistentDataType.BYTE, (byte) 1);
        });

        ItemDisplay display = loc.getWorld().spawn(loc, ItemDisplay.class, d -> {
            ItemStack model = new ItemStack(Material.PAPER);
            model.setData(DataComponentTypes.ITEM_MODEL, NAILS_MODEL);
            d.setItemStack(model);
            d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
            // the model is drawn centred on the origin, the floor of it is 0.5 blocks lower
            d.setTransformation(new Transformation(new Vector3f(0f, 0.5f, 0f), new Quaternionf(),
                    new Vector3f(1f, 1f, 1f), new Quaternionf()));
            d.setPersistent(true);
            d.setRotation(yaw, 0f);
            d.setBrightness(brightness);
        });

        hitbox.getPersistentDataContainer().set(displayKey, PersistentDataType.STRING, display.getUniqueId().toString());
    }

    private void breakNails(Entity nails, boolean drop) {
        Location loc = nails.getLocation().add(0, 0.1, 0);

        ItemDisplay display = getDisplay(nails);
        if (display != null)
            display.remove();
        nails.remove();

        loc.getWorld().playSound(loc, Sound.BLOCK_CHAIN_BREAK, 1f, 1.4f);
        if (drop)
            loc.getWorld().dropItemNaturally(loc, createItem());
    }

    // ---------------------------------------------------------------- events

    @EventHandler
    public void onPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND)
            return;
        if (!isNailsItem(event.getItem()))
            return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        Block target = event.getClickedBlock().getRelative(event.getBlockFace());
        Block below = target.getRelative(0, -1, 0);

        // they lie flat on top of a full block
        if (!target.isPassable() || below.isPassable() || below.getBoundingBox().getMaxY() < below.getY() + 1.0)
            return;

        Location loc = target.getLocation().add(0.5, 0, 0.5);

        // one per block
        for (Entity e : loc.getWorld().getNearbyEntities(loc, 0.5, 0.5, 0.5))
            if (isNails(e))
                return;

        spawn(loc);
        loc.getWorld().playSound(loc, Sound.BLOCK_CHAIN_PLACE, 1f, 1.2f);
        player.swingMainHand();

        if (player.getGameMode() != GameMode.CREATIVE)
            event.getItem().subtract();
    }

    /**
     * Hit it to pick it up, like a boat: the damage of the player's attack (bare hand 1, swords and axes 4 or more,
     * with the held item's modifiers) adds up on the nails until HEALTH is reached.
     * So it takes 3 hits with the hand and a single one with a sword or an axe.
     */
    @EventHandler
    public void onPunch(PrePlayerAttackEntityEvent event) {
        Entity nails = event.getAttacked();
        if (!isNails(nails))
            return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        Location loc = nails.getLocation().add(0, 0.1, 0);
        loc.getWorld().playSound(loc, Sound.BLOCK_CHAIN_HIT, 1f, 0.75f);

        if (player.getGameMode() == GameMode.CREATIVE) {
            breakNails(nails, false);
            return;
        }

        AttributeInstance attack = player.getAttribute(Attribute.ATTACK_DAMAGE);
        double damage = attack == null ? 1.0 : attack.getValue();

        double left = nails.getPersistentDataContainer().getOrDefault(healthKey, PersistentDataType.DOUBLE, HEALTH) - damage;
        if (left <= 0.0)
            breakNails(nails, true);
        else
            nails.getPersistentDataContainer().set(healthKey, PersistentDataType.DOUBLE, left);
    }

    /** Don't leave the model floating around if the hitbox is removed some other way. */
    @EventHandler
    public void onRemove(EntityRemoveEvent event) {
        if (event.getCause() == EntityRemoveEvent.Cause.UNLOAD || !isNails(event.getEntity()))
            return;

        ItemDisplay display = getDisplay(event.getEntity());
        if (display != null)
            display.remove();
    }

    // ---------------------------------------------------------------- hurting

    /** Is the entity standing in nails? (used to slow the wheelchair down) */
    public boolean isOnNails(Entity entity) {
        for (Entity e : entity.getNearbyEntities(1, 1, 1))
            if (isNails(e) && e.getBoundingBox().overlaps(entity.getBoundingBox()))
                return true;
        return false;
    }

    private void tick() {
        Set<Interaction> nearby = new HashSet<>();

        for (Player player : Bukkit.getOnlinePlayers())
            for (Entity e : player.getNearbyEntities(16, 8, 16))
                if (isNails(e))
                    nearby.add((Interaction) e);

        for (Interaction nails : nearby) {
            // the ground is gone -> they fall off as an item
            Block below = nails.getLocation().getBlock().getRelative(0, -1, 0);
            if (below.isPassable()) {
                breakNails(nails, true);
                continue;
            }

            for (Entity e : nails.getNearbyEntities(1, 1, 1)) {
                if (!(e instanceof LivingEntity living) || e instanceof ItemDisplay)
                    continue;
                if (e instanceof Player p && (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR))
                    continue;
                if (!living.getBoundingBox().overlaps(nails.getBoundingBox()))
                    continue;

                if (DisabledSMP.plugin.wheelchairManager.isWheelchair(living)) {
                    // no damage event: even without a source vanilla shoves the victim around, and the wheelchair
                    // is supposed to roll through slowly. Just take the health away.
                    double left = living.getHealth() - DAMAGE;
                    if (left <= 0)
                        living.setHealth(0);
                    else
                        living.setHealth(left);
                } else {
                    living.damage(DAMAGE);
                }
                living.getWorld().playSound(living.getLocation(), Sound.BLOCK_CHAIN_STEP, 1f, 1f);
            }
        }
    }
}
