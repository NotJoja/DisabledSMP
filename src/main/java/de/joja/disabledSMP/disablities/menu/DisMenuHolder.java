package de.joja.disabledSMP.disablities.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class DisMenuHolder implements InventoryHolder {

    DisMenuType menuType;

    public DisMenuHolder(DisMenuType menuType) {
        this.menuType = menuType;
    }



    public DisMenuType getMenuType() {
        return menuType;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

}
