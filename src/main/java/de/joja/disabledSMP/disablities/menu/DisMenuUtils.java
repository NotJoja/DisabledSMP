package de.joja.disabledSMP.disablities.menu;

import de.joja.disabledSMP.disablities.Disability;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.utils.ConfigManager.CONFIG_LANGUAGE;

public class DisMenuUtils {

    public static Inventory createDisabilitiesMenu(Player player, String menuName, DisMenuType menuType) {

        List<Disability> disabilities = null;
        switch (menuType) {
            case SHOW_ALL -> disabilities = Arrays.asList(Disability.values());
            case SHOW_MINE -> disabilities = plugin.disabilityMap.get(player.getUniqueId());
        }

        Inventory menuInv = Bukkit.createInventory(
                new DisMenuHolder(menuType),
                27,
                Component.text(menuName)
        );

        ItemStack[] disabilityItems = new ItemStack[disabilities.size()];
        for (int i = 0; i < disabilityItems.length; i++) {

            ItemStack disItem = new ItemStack(Material.IRON_NUGGET, 1);
            ItemMeta meta = disItem.getItemMeta();

            if (CONFIG_LANGUAGE.equals("en")) {
                meta.displayName(createTextComponent(disabilities.get(i).enName, 237, 64, 87));
                String[] descriptionLinesStrings = disabilities.get(i).enDescription.split("&");
                List<Component> descriptionLinesComps =
                        createTextLineComponents(descriptionLinesStrings, 255, 255, 255);
                meta.lore(descriptionLinesComps);
            } else if (CONFIG_LANGUAGE.equals("de")) {
                meta.displayName(createTextComponent(disabilities.get(i).deName, 237, 64, 87));
                String[] descriptionLinesStrings = disabilities.get(i).deDescription.split("&");
                List<Component> descriptionLinesComps =
                        createTextLineComponents(descriptionLinesStrings, 255, 255, 255);
                meta.lore(descriptionLinesComps);
            }
            disItem.setItemMeta(meta);
            disabilityItems[i] = disItem;
        }

        menuInv.setContents(disabilityItems);
        return menuInv;
    }

    public static List<Component> createTextLineComponents(String[] lines, int r, int g, int b) {
        List<Component> linesComponents = new ArrayList<>();
        for (String lineString : lines)
            linesComponents.add(createTextComponent(lineString, r, g, b));
        return linesComponents;
    }

    public static Component createTextComponent(String text, int r, int g, int b) {
        return Component.text(text)
                .decoration(TextDecoration.ITALIC, false)
                .color(TextColor.color(r, g, b));
    }
}
