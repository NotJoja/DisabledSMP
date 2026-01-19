package de.joja.disabledSMP.disablities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.RGBLike;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static de.joja.disabledSMP.utils.ConfigManager.CONFIG_LANGUAGE;

public class DisabilityManager {

    public Map<UUID, List<Disability>> disabilityMap = new HashMap<>();

    public Inventory createMenu(Player player) {

        Inventory menuInv = Bukkit.createInventory(
                new DisabilityMenuHolder(),
                27,
                 Component.text("Disabilities of " + player.getName())
        );

        ItemStack[] disabilityItems = new ItemStack[Disability.values().length];
        for (int i = 0; i < disabilityItems.length; i++) {
            ItemStack disItem = new ItemStack(Material.IRON_NUGGET, 1);
            ItemMeta meta = disItem.getItemMeta();

            if (CONFIG_LANGUAGE.equals("en")) {

                meta.displayName(
                        Component.text(Disability.get(i).enName)
                                .decoration(TextDecoration.ITALIC, false)
                                .color(TextColor.color(237, 64, 87)));

                List<Component> descriptionLines = new ArrayList<>();
                for (String lineString : Disability.get(i).enDescription.split("&"))
                    descriptionLines.add(Component.text(lineString)
                            .decoration(TextDecoration.ITALIC, false)
                            .color(TextColor.color(255, 255, 255)));
                meta.lore(descriptionLines);

            } else if (CONFIG_LANGUAGE.equals("de")) {

                meta.displayName(Component.text(Disability.get(i).deName)
                        .decoration(TextDecoration.ITALIC, false)
                        .color(TextColor.color(237, 64, 87)));

                List<Component> descriptionLines = new ArrayList<>();
                for (String lineString : Disability.get(i).deDescription.split("&"))
                    descriptionLines.add(Component.text(lineString)
                            .decoration(TextDecoration.ITALIC, false)
                            .color(TextColor.color(255, 255, 255)));
                meta.lore(descriptionLines);

            }
            disItem.setItemMeta(meta);
            disabilityItems[i] = disItem;
        }

        menuInv.setContents(disabilityItems);

        return menuInv;
    }

}
