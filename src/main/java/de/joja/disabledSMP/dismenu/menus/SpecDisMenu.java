package de.joja.disabledSMP.dismenu.menus;

import de.joja.disabledSMP.disablities.CureManager;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.dismenu.ItemUtils;
import de.joja.disabledSMP.dismenu.clickables.DisabilityClickable;
import de.joja.disabledSMP.dismenu.clickables.MenuClickable;
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

public class SpecDisMenu extends Menu {

    public static final Key GO_BACK_ICON = Key.key("", "");
    public static final Key EXTRA_INFO_ICON = Key.key("", "");
    public static final Key GRAY_EXTRA_INFO_ICON = Key.key("", "");
    public static final Key CURE_RITUAL_ICON = Key.key("", "");
    public static final Key ADD_RITUAL_ICON = Key.key("", "");

    public static Inventory createSpecDisMenuInv(Player player, Disability disability) {
        SpecDisMenu menu = new SpecDisMenu(player.getUniqueId(), disability);
        Inventory menuInv = null;
        if (ConfigManager.CONFIG_LANGUAGE.equals("en"))
            menuInv = Bukkit.createInventory(menu, menu.size, Component.text(disability.enName + " Menu"));
        else if (ConfigManager.CONFIG_LANGUAGE.equals("de"))
            menuInv = Bukkit.createInventory(menu, menu.size, Component.text(disability.deName + " Menu"));
        menuInv.setContents(menu.items);
        return menuInv;
    }

    public final Disability disability;

    public SpecDisMenu(UUID uuid, Disability disability) {
        super(uuid);
        this.size = 9;
        this.disability = disability;
        initContents(size);
        createContents();
    }

    @Override
    protected void createContents() {

        MenuClickable goBack = player -> {
            Inventory mainDisMenuInv = ItemUtils.createMenuInv("Disabilities of " + player.getName(), new MainDisMenu(player.getUniqueId()));
            player.openInventory(mainDisMenuInv);
        };
        createMenuOption(0, "Go Back", GO_BACK_ICON, goBack);

        PlayerDisabilityExecutable withdrawCureExe =
                (player, disability) -> player.give(CureManager.createCureItem(disability));
        MenuClickable withdrawCure = new DisabilityClickable(disability, withdrawCureExe);
        createMenuOption(2, "Withdraw Cure", disability.cureIcon, withdrawCure);

        MenuClickable extraDisInfo = new DisabilityClickable(disability, SpecDisMenu::extraDisInfoExe);
        if (disability.specificInfoMenu == null)
            createMenuOption(4, "No Extra Disability Info", GRAY_EXTRA_INFO_ICON, extraDisInfo);
        else
            createMenuOption(4, "Extra Disability Info", EXTRA_INFO_ICON, extraDisInfo);

        PlayerDisabilityExecutable cureRitualExe = (player, disability) ->
                player.openInventory(CureRitualMenu.createCureRitualMenuInv(player, disability));
        DisabilityClickable cureRitual = new DisabilityClickable(disability, cureRitualExe);
        createMenuOption(6, "Cure Ritual", CURE_RITUAL_ICON, cureRitual);
    }

    private void createMenuOption(int i, String name, Key icon, MenuClickable clickable) {
        ItemStack item = new ItemStack(Material.IRON_NUGGET);
        ItemUtils.configureItem(item, icon, name, null, 0xFFFFFF, true);
        items[i] = item;
        clickables[i] = clickable;
    }

    private static void extraDisInfoExe(Player player, Disability disability) {
        if (disability.specificInfoMenu == null)
            return;

        if (ConfigManager.CONFIG_LANGUAGE.equals("en"))
            player.openInventory(ItemUtils
                    .createMenuInv(disability.enName + " Extra Info", disability.specificInfoMenu));
        else if (ConfigManager.CONFIG_LANGUAGE.equals("de"))
            player.openInventory(ItemUtils
                    .createMenuInv(disability.deName + " Extra Info", disability.specificInfoMenu));
    }

}
