package de.joja.disabledSMP.listeners;

import de.joja.disabledSMP.disablities.menu.DisMenuHolder;
import de.joja.disabledSMP.disablities.menu.DisMenuType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        if (!(event.getInventory().getHolder() instanceof DisMenuHolder holder))
            return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (holder.getMenuType() == DisMenuType.SHOW_ALL || holder.getMenuType() == DisMenuType.SHOW_MINE)
            return;

        // do here handle selection later, you hurensohn

        //ItemStack clicked = event.getCurrentItem();
        //if (clicked == null || clicked.getType().isAir()) return;
    }


}
