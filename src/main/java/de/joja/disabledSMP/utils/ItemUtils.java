package de.joja.disabledSMP.utils;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class ItemUtils {

    public static void configureItem(Lang lang, ItemStack item, Key texture,
                                     String enName, String deName,
                                     List<String> enDescription, List<String> deDescription,
                                     int rgb, boolean bold) {

        ItemMeta meta = item.getItemMeta();
        meta.displayName(createTextComponent(lang.pick(enName, deName), rgb, bold));
        List<String> description = lang.pick(enDescription, deDescription);
        if (description != null)
            meta.lore(createTextLinesComponents(description, 0xFFFFFF, false));
        item.setItemMeta(meta);
        item.setData(DataComponentTypes.ITEM_MODEL, texture);
    }

    /**
     * Item text that stays a translation key, so it shows in the language of whoever is looking at the item
     * (the client resolves it with the resource pack's lang files; the fallback is used if it can't).
     */
    public static Component translatable(String key, String fallback, int rgb, boolean bold, ComponentLike... args) {
        return Component.translatable(key, fallback, args)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, bold)
                .color(TextColor.color(rgb));
    }

    public static void configureItem(ItemStack item, Key texture, Component name, List<Component> lore) {
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        if (lore != null)
            meta.lore(lore);
        item.setItemMeta(meta);
        item.setData(DataComponentTypes.ITEM_MODEL, texture);
    }

    /** The translatable name of a disability (see disability.dismp.* in the lang files). */
    public static Component disabilityName(String enDataName, String enName) {
        return Component.translatable("disability.dismp." + enDataName, enName);
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
