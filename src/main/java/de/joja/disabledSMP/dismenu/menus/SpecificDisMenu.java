package de.joja.disabledSMP.dismenu.menus;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.dismenu.ItemUtils;
import de.joja.disabledSMP.dismenu.clickables.DisabilityClickable;
import de.joja.disabledSMP.dismenu.clickables.MenuClickable;
import de.joja.disabledSMP.dismenu.clickables.WithdrawCureClickable;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SpecificDisMenu extends Menu {

    public static final Key GO_BACK_ICON = Key.key("", "");
    public final Disability disability;

    public SpecificDisMenu(UUID uuid, Disability disability) {
        super(uuid);
        this.size = 9;
        this.disability = disability;
        initContents(size);
        createContents();
    }

    @Override
    protected void createContents() {

        MenuClickable goBack = player -> {
            Inventory mainDisMenuInv = ItemUtils.createMenuInv("Disabilities of " + player.getName(), new MainDisMenu(player.getUniqueId()));
            player.openInventory(mainDisMenuInv);
        };
        createMenuOption(0, "Go Back", GO_BACK_ICON, goBack);

        WithdrawCureClickable withdrawCure = new WithdrawCureClickable(disability);
        createMenuOption(1, "Withdraw Cure", disability.cureIcon, withdrawCure);
    }

    private void createMenuOption(int i, String name, Key icon, MenuClickable clickable) {
        ItemStack item = new ItemStack(Material.IRON_NUGGET);
        ItemUtils.configureItem(item, icon, name, null, 0xFFFFFF);
        items[i] = item;
        clickables[i] = clickable;
    }

}
