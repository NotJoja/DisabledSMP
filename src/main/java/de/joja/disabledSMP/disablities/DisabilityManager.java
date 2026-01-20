package de.joja.disabledSMP.disablities;

import de.joja.disabledSMP.disablities.menu.DisMenuHolder;
import de.joja.disabledSMP.disablities.menu.DisMenuType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

    public Inventory createDisabilitiesMenu(Player player, String menuName, DisMenuType menuType) {

        System.out.println("create thingy now");

        List<Disability> disabilities = null;
        switch (menuType) {
            case SHOW_ALL -> disabilities = Arrays.asList(Disability.values());
            case SHOW_MINE -> disabilities = disabilityMap.get(player.getUniqueId());
        }

       System.out.println("past switch");

        Inventory menuInv = Bukkit.createInventory(
                new DisMenuHolder(menuType),
                27,
                Component.text(menuName)
        );

        System.out.println("created menu inv kinda");

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

        System.out.println("past contents mading");

        menuInv.setContents(disabilityItems);

        return menuInv;
    }

    private List<Component> createTextLineComponents(String[] lines, int r, int g, int b) {
        List<Component> linesComponents = new ArrayList<>();
        for (String lineString : lines)
            linesComponents.add(createTextComponent(lineString, r, g, b));
        return linesComponents;
    }

    private Component createTextComponent(String text, int r, int g, int b) {
        return Component.text(text)
                .decoration(TextDecoration.ITALIC, false)
                .color(TextColor.color(237, 64, 87));
    }

}
