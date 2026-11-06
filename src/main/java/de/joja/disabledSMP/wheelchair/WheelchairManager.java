package de.joja.disabledSMP.wheelchair;

import de.joja.disabledSMP.DisabledSMP;
import de.joja.disabledSMP.advancements.AdvancementManager;
import de.joja.disabledSMP.utils.ItemUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.HorseJumpEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

/**
 * Placeable, rideable wheelchair.
 *
 * Technically it is an invisible, saddled, tamed horse (they are the only vanilla mobs you steer with WASD
 * without a carrot on a stick) with an ItemDisplay showing the wheelchair model riding on it.
 *
 *  - speed:  horse ridden speed == MOVEMENT_SPEED attribute, same physics as a player -> 0.13 = sprinting speed
 *  - steps:  vanilla gives player-controlled mobs max(step_height, 1.0), so 1 block edges work
 *  - size:   model scaled by MODEL_SCALE, the horse is scaled so the rider sits on the model's seat
 */
public class WheelchairManager implements Listener {

    /** The item model of the wheelchair in the resource pack (assets/dismp/items/wheelchair.json). */
    public static final Key WHEELCHAIR_MODEL = Key.key("dismp", "wheelchair");

    // ---------------------------------------------------------------- tuning

    /** Same as a sprinting player (0.1 * 1.3). */
    private static final double SPEED = 0.13;

    /** Scale of the model. 1.0 = original size. */
    private static final float MODEL_SCALE = 0.75f;

    /**
     * Scale of the invisible horse (= hitbox and seat height). A rider sits at 1.44375 * scale blocks above the
     * horse, and the seat cushion of the model is 17.5/16 = 1.09375 blocks high (times MODEL_SCALE), so this
     * puts the rider on the seat whatever the model scale is.
     */
    private static final float HORSE_SCALE = 17.5f / 16f * MODEL_SCALE / 1.44375f;

    /**
     * An item display draws the model centred on its origin (model coordinate 8/16 = origin), so positions measured
     * in the model (0..16) have to be shifted by 0.5 blocks.
     * The seat cushion is x 1..14, z 3.5..16.5 and its top is at y 17.5 (all in 1/16 blocks).
     */
    private static final Vector3f SEAT_CENTER = new Vector3f(7.5f / 16f - 0.5f, 0f, 10f / 16f - 0.5f);
    private static final float SEAT_TOP_Y = 17.5f / 16f - 0.5f;

    /**
     * Where the model is placed in relation to the point the display is riding at (the rider's hip): the seat top
     * has to end up there. Increase/decrease if the player sits too low/high in the wheelchair.
     */
    private static final float MODEL_Y_OFFSET = -SEAT_TOP_Y * MODEL_SCALE;

    // ---------------------------------------------------------------- keys

    private final JavaPlugin plugin;
    private final NamespacedKey itemKey;      // on the item
    private final NamespacedKey chairKey;     // on the horse
    private final NamespacedKey displayKey;   // on the horse: uuid of its display

    public WheelchairManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "wheelchair_item");
        this.chairKey = new NamespacedKey(plugin, "wheelchair");
        this.displayKey = new NamespacedKey(plugin, "wheelchair_display");

        registerRecipe();

        // ItemDisplay passengers don't turn with their vehicle, so copy the horse's rotation every tick
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getVehicle() instanceof Horse horse && isWheelchair(horse)) {
                    ItemDisplay display = getDisplay(horse);
                    if (display != null)
                        display.setRotation(horse.getLocation().getYaw(), 0f);
                }
            }
        }, 1L, 1L);
    }

    // ---------------------------------------------------------------- recipe

    private void registerRecipe() {
        NamespacedKey key = new NamespacedKey(plugin, "wheelchair");

        // don't register it twice (e.g. after a reload)
        Bukkit.removeRecipe(key);

        ShapedRecipe recipe = new ShapedRecipe(key, createItem());
        recipe.shape(
                " L ",
                "#O#",
                "I I"
        );
        recipe.setIngredient('L', Material.LEATHER);
        recipe.setIngredient('#', Material.IRON_BARS);
        recipe.setIngredient('O', Material.OAK_STAIRS);
        recipe.setIngredient('I', Material.IRON_BLOCK);
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        Bukkit.addRecipe(recipe);

        // unlock it in the recipe book for everyone who is already online
        for (Player player : Bukkit.getOnlinePlayers())
            player.discoverRecipe(key);
    }

    /** ... and for everyone who joins later. */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().discoverRecipe(new NamespacedKey(plugin, "wheelchair"));
    }

    // ---------------------------------------------------------------- item

    public ItemStack createItem() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemUtils.configureItem(
                item, WHEELCHAIR_MODEL,
                ItemUtils.translatable("item.dismp.wheelchair", "Wheelchair", 0xC8C8C8, false),
                List.of(
                        ItemUtils.translatable("item.dismp.wheelchair.lore.1", "Right-click on the ground to place it,", 0xFFFFFF, false),
                        ItemUtils.translatable("item.dismp.wheelchair.lore.2", "right-click it to sit down.", 0xFFFFFF, false)
                )
        );
        item.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
        item.editPersistentDataContainer(pdc -> pdc.set(itemKey, PersistentDataType.BYTE, (byte) 1));
        return item;
    }

    private boolean isWheelchairItem(ItemStack item) {
        return item != null && item.getType() == Material.PAPER
                && item.getPersistentDataContainer().has(itemKey, PersistentDataType.BYTE);
    }

    public boolean isWheelchair(Entity entity) {
        return entity instanceof Horse
                && entity.getPersistentDataContainer().has(chairKey, PersistentDataType.BYTE);
    }

    private ItemDisplay getDisplay(Horse horse) {
        String uuid = horse.getPersistentDataContainer().get(displayKey, PersistentDataType.STRING);
        if (uuid == null)
            return null;
        return Bukkit.getEntity(UUID.fromString(uuid)) instanceof ItemDisplay display ? display : null;
    }

    // ---------------------------------------------------------------- spawning

    private Horse spawn(Location loc) {
        Horse horse = loc.getWorld().spawn(loc, Horse.class, h -> {
            h.getPersistentDataContainer().set(chairKey, PersistentDataType.BYTE, (byte) 1);

            h.setAdult();
            h.setAgeLock(true);
            h.setTamed(true);
            h.setInvisible(true);
            h.setSilent(true);
            h.setInvulnerable(true);
            h.setPersistent(true);
            h.setRemoveWhenFarAway(false);
            h.getInventory().setSaddle(invisibleSaddle());

            setBase(h, Attribute.MOVEMENT_SPEED, SPEED);
            setBase(h, Attribute.SCALE, HORSE_SCALE);
            setBase(h, Attribute.JUMP_STRENGTH, 0.0);
            setBase(h, Attribute.KNOCKBACK_RESISTANCE, 1.0);
            setBase(h, Attribute.STEP_HEIGHT, 1.0);
        });

        // no wandering, panicking, eating... it only moves when ridden
        Bukkit.getMobGoals().removeAllGoals(horse);

        ItemDisplay display = loc.getWorld().spawn(loc, ItemDisplay.class, d -> {
            ItemStack model = new ItemStack(Material.PAPER);
            model.setData(DataComponentTypes.ITEM_MODEL, WHEELCHAIR_MODEL);
            d.setItemStack(model);
            d.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
            d.setTransformation(modelTransformation());
            d.setTeleportDuration(3);
            d.setPersistent(true);
        });

        horse.getPersistentDataContainer().set(displayKey, PersistentDataType.STRING, display.getUniqueId().toString());
        horse.addPassenger(display);
        return horse;
    }

    /**
     * The horse has to be saddled to be steerable. An invisible horse still renders its saddle, so give it one whose
     * equipment asset doesn't exist in the resource pack -> nothing is rendered.
     */
    private static ItemStack invisibleSaddle() {
        ItemStack saddle = new ItemStack(Material.SADDLE);
        saddle.setData(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.SADDLE)
                .assetId(Key.key("dismp", "invisible"))
                .build());
        return saddle;
    }

    private static void setBase(Horse horse, Attribute attribute, double value) {
        AttributeInstance instance = horse.getAttribute(attribute);
        if (instance != null)
            instance.setBaseValue(value);
    }

    /**
     * Item displays draw the model turned by 180 degrees, so the model's front (-Z) ends up facing the entity's
     * front (+Z) without any rotation of our own. That built-in turn also flips the model around the origin,
     * so the seat centre is moved back by the unflipped amount.
     */
    private static Transformation modelTransformation() {
        Vector3f translation = new Vector3f(
                SEAT_CENTER.x * MODEL_SCALE,
                MODEL_Y_OFFSET,
                SEAT_CENTER.z * MODEL_SCALE
        );

        return new Transformation(
                translation,
                new Quaternionf(),
                new Vector3f(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE),
                new Quaternionf()
        );
    }

    private void mount(Player player, Horse horse) {
        // the rider has to be the first passenger, otherwise the horse can't be controlled
        ItemDisplay display = getDisplay(horse);
        if (display != null)
            horse.removePassenger(display);

        if (!horse.addPassenger(player))
            plugin.getLogger().warning("Could not seat " + player.getName() + " in the wheelchair (mount was refused)");

        if (display != null)
            horse.addPassenger(display);

        DisabledSMP.plugin.advancementManager.grant(player, AdvancementManager.WHEELCHAIR);
    }

    private void pickUp(Player player, Horse horse) {
        Location loc = horse.getLocation();

        ItemDisplay display = getDisplay(horse);
        if (display != null)
            display.remove();
        horse.remove();

        // like a boat: nothing drops in creative, otherwise the item drops where the chair stood
        if (player.getGameMode() != GameMode.CREATIVE)
            loc.getWorld().dropItemNaturally(loc.add(0, 0.5, 0), createItem());
    }

    // ---------------------------------------------------------------- events

    /** Place it like a boat. */
    @EventHandler
    public void onPlace(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND)
            return;
        if (!isWheelchairItem(event.getItem()))
            return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        Block target = event.getClickedBlock().getRelative(event.getBlockFace());
        Block above = target.getRelative(0, 1, 0);

        if (!target.isPassable() || !above.isPassable())
            return;

        Location loc = target.getLocation().add(0.5, 0, 0.5);
        loc.setYaw(player.getLocation().getYaw());

        spawn(loc);
        player.swingMainHand();

        if (player.getGameMode() != GameMode.CREATIVE)
            event.getItem().subtract();
    }

    /** Sit down. */
    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        if (!isWheelchair(event.getRightClicked()))
            return;

        // this also stops vanilla (opening the horse inventory, feeding...)
        event.setCancelled(true);

        // the client may send an "interact at" and/or an "interact" event, react to whichever comes first:
        // after mounting the player is in a vehicle, so the second one is ignored
        if (event.getHand() != EquipmentSlot.HAND)
            return;

        Horse horse = (Horse) event.getRightClicked();
        Player player = event.getPlayer();

        if (player.isSneaking() || player.isInsideVehicle())
            return;

        boolean taken = horse.getPassengers().stream().anyMatch(e -> e instanceof Player);
        if (!taken)
            mount(player, horse);
    }

    /** Punch it to pick it up again. */
    @EventHandler
    public void onPunch(PrePlayerAttackEntityEvent event) {
        if (!isWheelchair(event.getAttacked()))
            return;

        event.setCancelled(true);
        pickUp(event.getPlayer(), (Horse) event.getAttacked());
    }

    /** No jumping with the wheelchair. */
    @EventHandler
    public void onJump(HorseJumpEvent event) {
        if (isWheelchair(event.getEntity()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onTransform(EntityTransformEvent event) {
        if (isWheelchair(event.getEntity()))
            event.setCancelled(true);
    }

    /** Don't leave the model floating around if the chair gets killed, voided, ... */
    @EventHandler
    public void onRemove(EntityRemoveEvent event) {
        if (event.getCause() == EntityRemoveEvent.Cause.UNLOAD || !isWheelchair(event.getEntity()))
            return;

        ItemDisplay display = getDisplay((Horse) event.getEntity());
        if (display != null)
            display.remove();
    }
}
