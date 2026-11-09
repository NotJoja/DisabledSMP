package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import de.joja.disabledSMP.utils.ItemUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public class CoagulationDisorder extends DisabilityHandler {

    public CoagulationDisorder() {
        this.enName = "Coagulation Disorder";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "If you get damaged, you won't regenerate,",
                "unless you use bandages"
        );

        this.deName = "Gerinnungsstörung";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du kannst dich nur regenerieren,",
                "wenn du Bandagen benutzt"
        );

        initIcons();
        Bukkit.getScheduler().runTaskLater(plugin, this::registerBandageRecipe, 20);
    }

    private final NamespacedKey bandageKey = new NamespacedKey("dismp", "bandage_key");
    private final NamespacedKey recipeKey = new NamespacedKey("dismp", "bandage_recipe");
    private final Set<UUID> healingPlayers = new HashSet<>();

    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event) {

        if (!(event.getEntity() instanceof Player player)) return;
        if (!hasDis(player, Disability.COAGULATION_DISORDER)) return;
        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED)
            return;

        if (healingPlayers.contains(player.getUniqueId()))
            return;

        event.setAmount(event.getAmount() * 0.65);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) return;
            int food = player.getFoodLevel();
            if (player.getSaturation() < 19)
                player.setFoodLevel(Math.max(0, food - 1));
            player.setSaturation(Math.max(0, player.getSaturation() - 2));
        });
    }

    private void registerBandageRecipe() {
        Bukkit.removeRecipe(recipeKey);

        ItemStack bandage = createBandage();
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, bandage);
        recipe.shape(
                "#P#",
                "PRP",
                "#P#"
        );
        recipe.setIngredient('P', Material.PAPER);
        recipe.setIngredient('R', Material.RED_WOOL);
        Bukkit.addRecipe(recipe);

        // unlock it in the recipe book for everyone who is already online
        for (Player player : Bukkit.getOnlinePlayers())
            player.discoverRecipe(recipeKey);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().discoverRecipe(recipeKey);
    }

    // Create bandage item
    public ItemStack createBandage() {
        ItemStack bandage = new ItemStack(Material.MILK_BUCKET);
        ItemMeta meta = bandage.getItemMeta();

        meta.setDisplayName("Bandage");
        meta.setItemModel(new NamespacedKey("dismp", "bandage"));

        meta.getPersistentDataContainer().set(bandageKey, PersistentDataType.BYTE, (byte) 1);
        bandage.setItemMeta(meta);
        bandage.setData(DataComponentTypes.MAX_STACK_SIZE, 16);

        return bandage;
    }



    @EventHandler
    public void onBandageUse(PlayerInteractEvent event) {

        if (event.getHand() == null) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();

        if (item == null || !item.hasItemMeta()) return;

        if (!item.getItemMeta().getPersistentDataContainer().has(
                bandageKey, PersistentDataType.BYTE)) return;

        event.setCancelled(true);

        Player player = event.getPlayer();

        if (activePuzzles.containsKey(player.getUniqueId())) return;

        openBandagePuzzle(player, event.getHand());
    }

    private void openBandagePuzzle(Player player, EquipmentSlot hand) {

        Inventory inv = Bukkit.createInventory(
                null,
                27,
                "§c§lTreat Your Wounds"
        );

        // Background
        ItemStack background = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bgMeta = background.getItemMeta();
        bgMeta.setDisplayName(" ");
        background.setItemMeta(bgMeta);

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, background);
        }

        // Randomly choose 3-5 red wool targets
        Random random = new Random();

        int targetCount = 3 + random.nextInt(3);

        PuzzleSession session = new PuzzleSession(inv, hand);

        while (session.remainingSlots.size() < targetCount) {
            int slot = random.nextInt(27);
            session.remainingSlots.add(slot);
            session.targetSlots.add(slot);
        }

        ItemStack redWool = new ItemStack(Material.RED_WOOL);
        ItemMeta woolMeta = redWool.getItemMeta();
        woolMeta.setDisplayName("§cWound");
        woolMeta.setLore(Arrays.asList(
                "§7Click to treat this wound!"
        ));
        redWool.setItemMeta(woolMeta);

        for (int slot : session.remainingSlots) {
            inv.setItem(slot, redWool);
        }

        activePuzzles.put(player.getUniqueId(), session);

        player.openInventory(inv);

        player.playSound(
                player.getLocation(),
                Sound.UI_BUTTON_CLICK,
                1f,
                1f
        );
    }

    @EventHandler
    public void onPuzzleClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) return;

        PuzzleSession session = activePuzzles.get(player.getUniqueId());

        if (session == null) return;

        if (event.getView().getTopInventory() != session.inventory)return;

        event.setCancelled(true);

        if (event.getClickedInventory() != session.inventory) return;

        int slot = event.getRawSlot();

        if (!session.remainingSlots.contains(slot)) {

            // Wrong click: reset all targets
            session.remainingSlots.clear();
            session.remainingSlots.addAll(session.targetSlots);

            ItemStack wool = createWoundItem();

            for (int target : session.targetSlots) {
                session.inventory.setItem(target, wool);
            }

            player.sendMessage("§cYou missed a wound! Try again.");
            player.playSound(
                    player.getLocation(),
                    Sound.ENTITY_VILLAGER_NO,
                    1f,
                    1f
            );

            return;
        }

        session.remainingSlots.remove(slot);
        session.inventory.setItem(slot, null);

        player.playSound(
                player.getLocation(),
                Sound.UI_BUTTON_CLICK,
                1f,
                1.5f
        );

        if (session.remainingSlots.isEmpty()) {
            finishBandage(player, session);
        }
    }

    private void finishBandage(Player player, PuzzleSession session) {

        UUID uuid = player.getUniqueId();

        // Remove puzzle before closing inventory
        activePuzzles.remove(uuid);

        player.closeInventory();

        // Get the original hand
        ItemStack held;

        if (session.hand == EquipmentSlot.HAND) {
            held = player.getInventory().getItemInMainHand();
        } else {
            held = player.getInventory().getItemInOffHand();
        }

        // Make sure the player still has a bandage
        if (held == null || !held.hasItemMeta()) return;

        if (!held.getItemMeta().getPersistentDataContainer().has(
                bandageKey, PersistentDataType.BYTE)) return;

        if (player.getGameMode() != GameMode.CREATIVE) {

            if (held.getAmount() <= 1) {
                held = new ItemStack(Material.AIR);
            } else {
                held.setAmount(held.getAmount() - 1);
            }

            if (session.hand == EquipmentSlot.HAND) {
                player.getInventory().setItemInMainHand(held);
            } else {
                player.getInventory().setItemInOffHand(held);
            }
        }

        player.sendMessage("§a§lYou successfully treated your wounds!");
        player.playSound(
                player.getLocation(),
                Sound.ENTITY_PLAYER_BURP,
                1f,
                1f
        );

        healingPlayers.add(uuid);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            healingPlayers.remove(uuid);
            player.sendMessage("§cYour bandage has worn off!");
        }, 20L * 90);
    }

    @EventHandler
    public void onPuzzleClose(InventoryCloseEvent event) {

        if (!(event.getPlayer() instanceof Player player)) return;

        PuzzleSession session = activePuzzles.get(player.getUniqueId());

        if (session == null) return;

        if (event.getInventory() != session.inventory) return;

        activePuzzles.remove(player.getUniqueId());

        player.sendMessage("§cYou stopped treating your wounds.");
    }

    @EventHandler
    public void onPuzzleDrag(InventoryDragEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) return;

        PuzzleSession session = activePuzzles.get(player.getUniqueId());

        if (session == null) return;

        if (event.getView().getTopInventory() == session.inventory) {
            event.setCancelled(true);
        }
    }



    private final Map<UUID, PuzzleSession> activePuzzles = new HashMap<>();

    private static class PuzzleSession {
        Inventory inventory;
        Set<Integer> remainingSlots = new HashSet<>();
        Set<Integer> targetSlots = new HashSet<>();
        EquipmentSlot hand;

        PuzzleSession(Inventory inventory, EquipmentSlot hand) {
            this.inventory = inventory;
            this.hand = hand;
        }
    }

    private ItemStack createWoundItem() {

        ItemStack wool = new ItemStack(Material.RED_WOOL);

        ItemMeta meta = wool.getItemMeta();

        meta.setDisplayName("§cWound");
        meta.setLore(Arrays.asList(
                "§7Click to treat this wound!"
        ));

        wool.setItemMeta(meta);

        return wool;
    }

}

