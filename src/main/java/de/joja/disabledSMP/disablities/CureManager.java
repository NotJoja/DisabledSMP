package de.joja.disabledSMP.disablities;

import de.joja.disabledSMP.dismenu.ItemUtils;
import de.joja.disabledSMP.utils.DisConfig;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public abstract class CureManager {

    public static final int CURE_RGB = 0x82C44F;

    public static ItemStack createCureItem(Disability disability) {
        ItemStack cureItem = new ItemStack(Material.POTATO);

        if (DisConfig.CONFIG_LANGUAGE.equals("en"))
            ItemUtils.configureItem(cureItem, disability.cureIcon, disability.enName + " Cure", null, CURE_RGB, false);
        else if (DisConfig.CONFIG_LANGUAGE.equals("de"))
            ItemUtils.configureItem(cureItem, disability.cureIcon, disability.deName + " Cure", null, CURE_RGB, false);

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
