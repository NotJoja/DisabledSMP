package de.joja.disabledSMP.dismenu.menus;

import de.joja.disabledSMP.dismenu.clickables.MenuClickable;
import de.joja.disabledSMP.utils.DisConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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

    public abstract Inventory createMenuInv();

    protected Inventory createMenuInvHelper(String enName, String deName) {
        Inventory menuInv = null;
        if (DisConfig.CONFIG_LANGUAGE.equals("en"))
            menuInv = Bukkit.createInventory(this, this.size, Component.text(enName));
        else if (DisConfig.CONFIG_LANGUAGE.equals("de"))
            menuInv = Bukkit.createInventory(this, this.size, Component.text(deName));
        menuInv.setContents(this.items);
        return menuInv;
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
