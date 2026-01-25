package de.joja.disabledSMP.dismenu.menus;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.dismenu.ItemUtils;
import de.joja.disabledSMP.dismenu.clickables.DisabilityClickable;
import de.joja.disabledSMP.dismenu.clickables.PlayerDisabilityExecutable;
import de.joja.disabledSMP.utils.ConfigManager;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class CureRitualMenu extends Menu {

    public static final Key GO_BACK_ICON = Key.key("", "");

    public static Inventory createCureRitualMenuInv(Player player, Disability disability) {
        CureRitualMenu menu = new CureRitualMenu(player.getUniqueId(), disability);
        Inventory menuInv = null;
        if (ConfigManager.CONFIG_LANGUAGE.equals("en"))
            menuInv = Bukkit.createInventory(menu, menu.size, Component.text(disability.enName + " Cure Ritual"));
        else if (ConfigManager.CONFIG_LANGUAGE.equals("de"))
            menuInv = Bukkit.createInventory(menu, menu.size, Component.text(disability.deName + " Heilung Ritual"));
        menuInv.setContents(menu.items);
        return menuInv;
    }

    public final Disability disability;

    public CureRitualMenu(UUID uuid, Disability disability) {
        super(uuid);
        this.size = 6*9;
        this.disability = disability;
        initContents(size);
        createContents();
    }

    @Override
    protected void createContents() {

        ItemStack goBackItem = new ItemStack(Material.IRON_NUGGET);
        ItemUtils.configureItem(goBackItem, GO_BACK_ICON, "Go Back", null, 0xFFFFFF, true);
        setItem(0, 0, goBackItem);
        PlayerDisabilityExecutable goBackExe = (player, disability) ->
                player.openInventory(SpecDisMenu.createSpecDisMenuInv(player, disability));
        DisabilityClickable goBack = new DisabilityClickable(disability, goBackExe);
        clickables[0] = goBack;

        setItemRelativeToCenter(0, 0, new ItemStack(Material.CAULDRON));

        setItemRelativeToCenter( 0,  1, new ItemStack(disability.cureDisRitual[0]));
        setItemRelativeToCenter( 0, -1, new ItemStack(disability.cureDisRitual[0]));
        setItemRelativeToCenter( 1,  0, new ItemStack(disability.cureDisRitual[0]));
        setItemRelativeToCenter(-1, -0, new ItemStack(disability.cureDisRitual[0]));

        setItemRelativeToCenter( 1,  1, new ItemStack(disability.cureDisRitual[1]));
        setItemRelativeToCenter( 1, -1, new ItemStack(disability.cureDisRitual[1]));
        setItemRelativeToCenter(-1,  1, new ItemStack(disability.cureDisRitual[1]));
        setItemRelativeToCenter(-1, -1, new ItemStack(disability.cureDisRitual[1]));
    }

    protected void setItem(int x, int y, ItemStack item) {
        this.items[x + y*9] = item;
    }

    protected void setItemRelativeToCenter(int x, int y, ItemStack item) {
        int rx = x + 4;
        int ry = y + 3;
        this.items[rx + ry*9] = item;
    }
}
