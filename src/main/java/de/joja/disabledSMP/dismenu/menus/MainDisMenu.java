package de.joja.disabledSMP.dismenu.menus;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.dismenu.ItemUtils;
import de.joja.disabledSMP.dismenu.clickables.DisabilityClickable;
import de.joja.disabledSMP.utils.ConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public class MainDisMenu extends Menu {

    public static final int DIS_RED_RGB  = 0xED4057;
    public static final int DIS_GRAY_RGB = 0x6E6A6B;

    public MainDisMenu(UUID uuid) {
        super(uuid);
        this.size = 27;
        initContents(size);
        createContents();
    }

    @Override
    protected void createContents() {

        List<Disability> playerDisabilities = plugin.disabilityMap.get(uuid);

        for (int i = 0; i < Disability.values().length; i++) {

            Disability dis = Disability.get(i);
            ItemStack item = new ItemStack(Material.IRON_NUGGET);

            int rgb = playerDisabilities.contains(dis) ? DIS_RED_RGB : DIS_GRAY_RGB;

            if (ConfigManager.CONFIG_LANGUAGE.equals("en"))
                ItemUtils.configureItem(item, dis.icon, dis.enName, dis.enDescription, rgb, false);
            else if (ConfigManager.CONFIG_LANGUAGE.equals("de"))
                ItemUtils.configureItem(item, dis.icon, dis.deName, dis.deDescription, rgb, false);

            this.items[i] = item;
            this.clickables[i] = new DisabilityClickable(dis, MainDisMenu::onClickDisItem);
        }

    }

    private static void onClickDisItem(Player player, Disability disability) {
        Inventory menuInv = null;
        if (ConfigManager.CONFIG_LANGUAGE.equals("en"))
            menuInv = ItemUtils.createMenuInv(disability.enName, new SpecDisMenu(player.getUniqueId(), disability));
        else if (ConfigManager.CONFIG_LANGUAGE.equals("de"))
            menuInv = ItemUtils.createMenuInv(disability.deName, new SpecDisMenu(player.getUniqueId(), disability));
        player.openInventory(menuInv);
    }
}
