package de.joja.disabledSMP.dismenu.clickables;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.dismenu.ItemUtils;
import de.joja.disabledSMP.dismenu.menus.SpecificDisMenu;
import de.joja.disabledSMP.utils.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class DisabilityClickable implements MenuClickable {

    public Disability disability;

    public DisabilityClickable(Disability disability) {
        this.disability = disability;
    }

    @Override
    public void onClick(Player player) {
        Inventory menuInv = null;
        if (ConfigManager.CONFIG_LANGUAGE.equals("en"))
            menuInv = ItemUtils.createMenuInv(disability.enName, new SpecificDisMenu(player.getUniqueId(), disability));
        else if (ConfigManager.CONFIG_LANGUAGE.equals("de"))
            menuInv = ItemUtils.createMenuInv(disability.deName, new SpecificDisMenu(player.getUniqueId(), disability));
        player.openInventory(menuInv);
    }
}
