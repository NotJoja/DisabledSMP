package de.joja.disabledSMP.wheelchair;

import de.joja.disabledSMP.DisabledSMP;
import de.joja.disabledSMP.advancements.AdvancementManager;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import de.joja.disabledSMP.utils.ItemUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
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

    /**
     * Health of the wheelchair while someone sits in it (an empty one breaks with a single hit).
     * Player hits do vanilla damage: axes 9 (netherite 10, wooden/golden 7), diamond sword 7,
     * iron sword 6, ... so 18 means two axe hits or three diamond sword hits break it.
     */
    private static final double MAX_HEALTH = 18.0;

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

    /** How long an overweight player can sit in the wheelchair before it breaks. */
    private static final long COLLAPSE_DELAY_TICKS = 6;

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
            h.setPersistent(true);
            h.setRemoveWhenFarAway(false);
            h.getInventory().setSaddle(invisibleSaddle());

            setBase(h, Attribute.MOVEMENT_SPEED, SPEED);
            setBase(h, Attribute.SCALE, HORSE_SCALE);
            setBase(h, Attribute.JUMP_STRENGTH, 0.0);
            setBase(h, Attribute.KNOCKBACK_RESISTANCE, 1.0);
            setBase(h, Attribute.MAX_HEALTH, MAX_HEALTH);
            h.setHealth(MAX_HEALTH);
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
        if (taken)
            return;

        mount(player, horse);

        // too heavy: they can sit for a split second, then the wheelchair gives way
        if (DisabilityHandler.hasDis(player, Disability.OVERWEIGHT))
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (horse.isValid())
                    destroy(horse);
            }, COLLAPSE_DELAY_TICKS);
    }

    /** Destroys the wheelchair: tool break sound, some particles and the crafting materials drop. */
    private void destroy(Horse horse) {
        Location loc = horse.getLocation().add(0, 0.6, 0);

        // what the recipe needs (see registerRecipe)
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.LEATHER));
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.IRON_BARS, 2));
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.OAK_STAIRS));
        loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.IRON_BLOCK, 2));

        loc.getWorld().playSound(loc, Sound.ENTITY_ITEM_BREAK, 1f, 1f);
        loc.getWorld().spawnParticle(Particle.ITEM, loc, 30, 0.3, 0.3, 0.3, 0.05, new ItemStack(Material.IRON_BLOCK));
        loc.getWorld().spawnParticle(Particle.ITEM, loc, 15, 0.3, 0.3, 0.3, 0.05, new ItemStack(Material.LEATHER));

        // remove() doesn't fire a death event (no wheelchair item), and onRemove() takes the model with it
        horse.remove();
    }

    /**
     * Chairs placed before it had health are invulnerable: make them breakable now. The attack is still being
     * processed, so this already applies to the hit that triggered the event.
     */
    @EventHandler
    public void onPunch(PrePlayerAttackEntityEvent event) {
        if (!isWheelchair(event.getAttacked()) || !event.getAttacked().isInvulnerable())
            return;

        Horse horse = (Horse) event.getAttacked();
        horse.setInvulnerable(false);
        setBase(horse, Attribute.MAX_HEALTH, MAX_HEALTH);
        horse.setHealth(MAX_HEALTH);
    }

    /** Only player hits hurt it, everything else (fire, explosions, arrows, falling, ...) is ignored. */
    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!isWheelchair(event.getEntity()))
            return;

        boolean playerHit = event instanceof EntityDamageByEntityEvent byEntity
                && byEntity.getDamager() instanceof Player
                && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK;

        if (!playerHit) {
            event.setCancelled(true);
            return;
        }

        Player attacker = (Player) ((EntityDamageByEntityEvent) event).getDamager();
        Horse horse = (Horse) event.getEntity();

        // an empty wheelchair breaks with a single hit, like picking up a boat.
        // Only while someone sits in it, it has health (also for creative players, they just get no item).
        boolean occupied = horse.getPassengers().stream().anyMatch(e -> e instanceof Player);
        if (!occupied) {
            event.setDamage(horse.getHealth() + 1);
            return;
        }

        // the horse is silent and invisible, so give some feedback
        horse.getWorld().playSound(horse.getLocation(), Sound.BLOCK_CHAIN_HIT, 1f, 1f);
    }

    /** Broken: drops the wheelchair item (not in creative) instead of the horse's loot. */
    @EventHandler
    public void onBreak(EntityDeathEvent event) {
        if (!isWheelchair(event.getEntity()))
            return;

        event.getDrops().clear();
        event.setDroppedExp(0);

        // remove the model right away, don't wait for the remove event
        ItemDisplay display = getDisplay((Horse) event.getEntity());
        if (display != null)
            display.remove();

        Player killer = event.getEntity().getKiller();
        if (killer != null && killer.getGameMode() != GameMode.CREATIVE)
            event.getDrops().add(createItem());

        Location loc = event.getEntity().getLocation();
        loc.getWorld().playSound(loc, Sound.BLOCK_CHAIN_BREAK, 1f, 1f);
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
