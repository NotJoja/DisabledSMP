package de.joja.disabledSMP.disablities.handlers;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import de.joja.disabledSMP.utils.ItemUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public class ParalyzedArm extends DisabilityHandler {

    public ParalyzedArm() {
        this.enName = "Paralyzed Arm";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You can't use your off-hand and half of your hotbar"
        );

        this.deName = "Gelähmter Arm";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du kannst nicht deine Off-Hand benutzen",
                "und die Hälfte deiner Hotbar"
        );

        initIcons();

        // The client can lose track of a BLOCKED item (e.g. swapping to a blocked slot and dropping right away: the
        // client already removed it, the server never did), so every few seconds the inventory is sent again.
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers())
                if (hasDis(player, Disability.PARALYZED_ARM)) {
                    repairBlockedItems(player);
                    player.updateInventory();
                }
        }, RESYNC_TICKS, RESYNC_TICKS);
    }

    private static ItemStack createBlockedItem() {
        ItemStack blocked = new ItemStack(Material.CARROT_ON_A_STICK);
        ItemUtils.configureItem(blocked, Key.key("minecraft", "barrier"),
                ItemUtils.translatable("item.dismp.blocked", "BLOCKED", 0xF00A0A, true), null);
        return blocked;
    }

    /** Puts a fresh BLOCKED item where one was changed (renamed, other texture, ...) by something else. */
    private static void repairBlockedItems(Player player) {
        ItemStack fresh = createBlockedItem();
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < 4; i++)
            if (isBlockedItem(inv.getItem(i)) && !fresh.isSimilar(inv.getItem(i)))
                inv.setItem(i, fresh);
        if (isBlockedItem(inv.getItemInOffHand()) && !fresh.isSimilar(inv.getItemInOffHand()))
            inv.setItemInOffHand(fresh);
    }

    /** The BLOCKED placeholder items (other disabilities must leave them alone: no renaming, moving or retexturing). */
    public static boolean isBlockedItem(ItemStack item) {
        return item != null && item.getType() == Material.CARROT_ON_A_STICK;
    }

    /** How often the inventory of players with a paralyzed arm is sent to them again. */
    private static final long RESYNC_TICKS = 40;

    /** The BLOCKED items can't be dropped (the client already hid it, so it is sent again right away). */
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!hasDis(player, Disability.PARALYZED_ARM))
            return;

        if (event.getItemDrop().getItemStack().getType() == Material.CARROT_ON_A_STICK) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, player::updateInventory);
        }
    }



    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (hasDis(player, Disability.PARALYZED_ARM))
            event.setCancelled(true);
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        int newSlot = event.getNewSlot();

        if (hasDis(player, Disability.PARALYZED_ARM) && newSlot < 4) {
            event.setCancelled(true);

            if (newSlot == 0)
                player.getInventory().setHeldItemSlot(4);
            else if (newSlot == 3)
                player.getInventory().setHeldItemSlot(8);
            else
                player.getInventory().setHeldItemSlot(6);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!hasDis(player, Disability.PARALYZED_ARM))
            return;

        boolean offHand = event.getHand() == EquipmentSlot.OFF_HAND;
        boolean blockedSlot = player.getInventory().getHeldItemSlot() < 4;
        if (offHand || blockedSlot)
            event.setCancelled(true);

        // A (powered or chained) shelf swaps the player's whole hotbar with what is on the shelf, which would put the
        // BLOCKED items on it and let anyone take them. Shelves can't be used at all.
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null
                && Tag.WOODEN_SHELVES.isTagged(event.getClickedBlock().getType()))
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;
        if (!hasDis(player, Disability.PARALYZED_ARM))
            return;

        ClickType click = event.getClick();
        int slot = event.getSlot();

        boolean numKeySwap = (click == ClickType.NUMBER_KEY) && (event.getHotbarButton() < 4);
        boolean offhandSwap = (click == ClickType.SWAP_OFFHAND);
        if (offhandSwap || numKeySwap)
            event.setCancelled(true);

        // slot numbers are per inventory (a crafting result or furnace slot is also "slot 0"),
        // so only the player's own hotbar slots 0-3 and off-hand slot 40 are locked.
        // Every click type is blocked there (shift-click, right-click, number keys, drop, ...),
        // otherwise the BLOCKED items could be moved around.
        boolean ownInventory = event.getClickedInventory() instanceof PlayerInventory;
        boolean lockedSlot = ownInventory && (slot < 4 || slot == 40);
        if (lockedSlot)
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        if (!hasDis(player, Disability.PARALYZED_ARM))
            return;
        List<ItemStack> drops = event.getDrops();
        for (int i = 0; i < drops.size(); i++) {
            if (drops.get(i).getType() == Material.CARROT_ON_A_STICK) {
                drops.remove(i);
                i--;
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerPostRespawnEvent event) {
        Player player = event.getPlayer();
        if (!hasDis(player, Disability.PARALYZED_ARM))
            return;

        ItemStack blocked = createBlockedItem();

        player.getInventory().setItemInOffHand(blocked);
        for (int i = 0; i < 4; i++)
            player.getInventory().setItem(i, blocked);
    }



    @Override
    public void addToPlayer(Player player) {

        for (int i = 0; i < 4; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null)
                continue;
            if (item.getType() != Material.CARROT_ON_A_STICK)
                player.dropItem(i);
        }

        if (player.getInventory().getItemInOffHand().getType() != Material.CARROT_ON_A_STICK)
            player.dropItem(EquipmentSlot.OFF_HAND);

        ItemStack blocked = createBlockedItem();

        player.getInventory().setItemInOffHand(blocked);
        for (int i = 0; i < 4; i++)
            player.getInventory().setItem(i, blocked);
    }

    @Override
    public void removeFromPlayer(Player player) {
        for (int i = 0; i < 4; i++)
            player.getInventory().setItem(i, new ItemStack(Material.AIR));
        player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
    }
}
