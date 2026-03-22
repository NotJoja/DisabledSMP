package de.joja.disabledSMP.utils;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class ItemUtils {

    public static void configureItem(ItemStack item, Key texture,
                                     String enName, String deName,
                                     List<String> enDescription, List<String> deDescription,
                                     int rgb, boolean bold) {

        ItemMeta meta = item.getItemMeta();
        if (DConfig.LANGUAGE.equals("en")) {
            meta.displayName(createTextComponent(enName, rgb, bold));
            if (enDescription != null)
                meta.lore(createTextLinesComponents(enDescription, 0xFFFFFF, false));
        } else if (DConfig.LANGUAGE.equals("de")) {
            meta.displayName(createTextComponent(deName, rgb, bold));
            if (deDescription != null)
                meta.lore(createTextLinesComponents(deDescription, 0xFFFFFF, false));
        }
        item.setItemMeta(meta);
        item.setData(DataComponentTypes.ITEM_MODEL, texture);
    }

    public static void configureItem(ItemStack item, Key texture, String name, int rgb, boolean bold) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(createTextComponent(name, rgb, bold));
        item.setItemMeta(meta);
        item.setData(DataComponentTypes.ITEM_MODEL, texture);
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
