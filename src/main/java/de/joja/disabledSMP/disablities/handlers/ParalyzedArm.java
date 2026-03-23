package de.joja.disabledSMP.disablities.handlers;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import de.joja.disabledSMP.utils.ItemUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ParalyzedArm extends DisabilityHandler {

    public ParalyzedArm() {
        this.enName = "Paralyzed Arm";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You can't use your off-hand and half of your hotbar"
        );

        this.deName = "Gelähmter Arm";
        this.deDataName = toDataName(enName);
        this.deDescription = List.of(
                "Du kannst nicht deine Off-Hand benutzen",
                "und die Hälfte deiner Hotbar"
        );

        initIcons();
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

        boolean blockedClickType = (click == ClickType.LEFT) || (click == ClickType.DROP);
        boolean blockSlot = (slot < 4 || slot == 40);
        if (blockedClickType && blockSlot)
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

        ItemStack blocked = new ItemStack(Material.CARROT_ON_A_STICK);
        ItemUtils.configureItem(blocked, Key.key("minecraft", "barrier"), "BLOCKED", 0xF00A0A, true);

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

        ItemStack blocked = new ItemStack(Material.CARROT_ON_A_STICK);
        ItemUtils.configureItem(blocked, Key.key("minecraft", "barrier"), "BLOCKED", 0xF00A0A, true);

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
