package de.joja.disabledSMP.dismenu.menus;

import de.joja.disabledSMP.dismenu.clickables.MenuClickable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class Menu implements InventoryHolder {

    public UUID uuid;

    public int size;
    public ItemStack[] items;
    public MenuClickable[] clickables;

    public Menu(UUID uuid) {
        this.uuid = uuid;
    }

    protected void initContents(int size) {
        this.items = new ItemStack[size];
        this.clickables = new MenuClickable[size];
    }

    protected abstract void createContents();

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
