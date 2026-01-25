package de.joja.disabledSMP.dismenu;

import de.joja.disabledSMP.dismenu.clickables.MenuClickable;
import de.joja.disabledSMP.dismenu.menus.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class MenuManager implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;

        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof Menu menu))
            return;

        event.setCancelled(true);
        int slot = event.getRawSlot();
        MenuClickable clickable = menu.clickables[slot];
        if (clickable != null)
            menu.clickables[slot].onClick(player);
    }
}

