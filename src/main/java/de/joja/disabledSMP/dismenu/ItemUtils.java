package de.joja.disabledSMP.dismenu;

import de.joja.disabledSMP.dismenu.menus.Menu;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class ItemUtils {

    public static Inventory createMenuInv(String menuName, Menu menu) {
        Inventory menuInv = Bukkit.createInventory(menu, menu.size, Component.text(menuName));
        menuInv.setContents(menu.items);
        return menuInv;
    }

    public static void configureItem(ItemStack item, Key texture, String name, List<String> description, int rgb, boolean bold) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(createTextComponent(name, rgb, bold));
        item.setData(DataComponentTypes.ITEM_MODEL, texture);
        if (description != null)
            meta.lore(createTextLinesComponents(description, 0xFFFFFF, bold));
        item.setItemMeta(meta);
    }

    public static List<Component> createTextLinesComponents(List<String> lines, int rgb, boolean bold) {
        List<Component> linesComponents = new ArrayList<>();
        for (String lineString : lines)
            linesComponents.add(createTextComponent(lineString, rgb, bold));
        return linesComponents;
    }

    public static Component createTextComponent(String text, int rgb, boolean bold) {
        return Component.text(text)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, bold)
                .color(TextColor.color(rgb));
    }

}
