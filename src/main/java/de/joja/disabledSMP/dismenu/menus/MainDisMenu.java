package de.joja.disabledSMP.dismenu.menus;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.dismenu.clickables.DisabilityClickable;
import de.joja.disabledSMP.utils.DConfig;
import de.joja.disabledSMP.utils.ItemUtils;
import net.kyori.adventure.key.Key;
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
    public Inventory createMenuInv() {
        return null;
    }

    @Override
    protected void createContents() {

        List<Disability> playerDisabilities = plugin.disabilityMap.get(uuid);

        for (int i = 0; i < Disability.values().length; i++) {

            Disability dis = Disability.get(i);
            ItemStack item = new ItemStack(Material.IRON_NUGGET);

            boolean playerHasDis = playerDisabilities.contains(dis);
            int rgb = playerHasDis ? DIS_RED_RGB : DIS_GRAY_RGB;
            Key icon = playerHasDis ? dis.icon : dis.grayIcon;

            ItemUtils.configureItem(item, icon, dis.enName, dis.deName, dis.enDescription, dis.deDescription, rgb, true);

            this.items[i] = item;
            this.clickables[i] = new DisabilityClickable(dis, MainDisMenu::onClickDisItem);
        }

    }

    private static void onClickDisItem(Player player, Disability disability) {
        Inventory menuInv = new SpecDisMenu(player.getUniqueId(), disability).createMenuInv();
        player.openInventory(menuInv);
    }
}
