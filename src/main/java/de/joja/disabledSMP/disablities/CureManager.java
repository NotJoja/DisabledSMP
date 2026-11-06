package de.joja.disabledSMP.disablities;

import de.joja.disabledSMP.utils.DConfig;
import de.joja.disabledSMP.utils.ItemUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.FoodProperties;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.utils.Icons.CURE_RGB;

public abstract class CureManager { ;

    public static ItemStack createCureItem(Disability disability) {
        ItemStack cureItem = new ItemStack(Material.POTATO);

        if (DConfig.LANGUAGE.equals("en"))
            ItemUtils.configureItem(cureItem, disability.cureIcon, disability.enName + " Cure", CURE_RGB, false);
        else if (DConfig.LANGUAGE.equals("de"))
            ItemUtils.configureItem(cureItem, disability.cureIcon, disability.deName + " Cure", CURE_RGB, false);

        // can be eaten even with a full hunger bar (and doesn't feed you either)
        cureItem.setData(DataComponentTypes.FOOD, FoodProperties.food()
                .nutrition(0)
                .saturation(0f)
                .canAlwaysEat(true)
                .build());

        ItemMeta meta = cureItem.getItemMeta();
        meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "cure"),
                PersistentDataType.INTEGER,
                disability.ordinal()
        );

        cureItem.setItemMeta(meta);
        return cureItem;
    }

}
