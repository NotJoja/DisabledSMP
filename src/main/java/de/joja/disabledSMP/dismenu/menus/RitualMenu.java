package de.joja.disabledSMP.dismenu.menus;

import de.joja.disabledSMP.disablities.CureManager;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.dismenu.ItemUtils;
import de.joja.disabledSMP.dismenu.clickables.DisabilityClickable;
import de.joja.disabledSMP.dismenu.clickables.PlayerDisabilityExecutable;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

import static de.joja.disabledSMP.dismenu.menus.SpecDisMenu.GO_BACK_ICON;

public class RitualMenu extends Menu {

    public final Disability disability;
    public final boolean cure;

    public RitualMenu(UUID uuid, Disability disability, boolean cure) {
        super(uuid);
        this.size = 6*9;
        this.disability = disability;
        this.cure = cure;
        initContents(size);
        createContents();
    }

    @Override
    public Inventory createMenuInv() {
        return createMenuInvHelper(disability.enName + " Cure Ritual", disability.deName + " Heilungs Ritual");
    }

    @Override
    protected void createContents() {

        ItemStack ritualInfoItem = new ItemStack(Material.IRON_NUGGET);
        if (cure)
            ItemUtils.configureItem(ritualInfoItem, disability.icon, "Cure Ritual of " + disability.enName, null, CureManager.CURE_RGB, false);
        items[4] = ritualInfoItem;

        ItemStack goBackItem = new ItemStack(Material.IRON_NUGGET);
        ItemUtils.configureItem(goBackItem, GO_BACK_ICON, "Go Back", null, 0xFFFFFF, true);
        items[0] = goBackItem;
        PlayerDisabilityExecutable goBackExe = (player, disability) ->
                player.openInventory(new SpecDisMenu(uuid, disability).createMenuInv());
        DisabilityClickable goBack = new DisabilityClickable(disability, goBackExe);
        clickables[0] = goBack;

        if (cure)
            setItemRelativeToCenter(items, 0, 0, new ItemStack(Material.CAULDRON));
        else
            setItemRelativeToCenter(items, 0, 0, new ItemStack(Material.LIGHT_WEIGHTED_PRESSURE_PLATE));

        if (cure) {
            setItemRelativeToCenter(items,  0,  1, new ItemStack(disability.ritual[0]));
            setItemRelativeToCenter(items,  0, -1, new ItemStack(disability.ritual[0]));
            setItemRelativeToCenter(items,  1,  0, new ItemStack(disability.ritual[0]));
            setItemRelativeToCenter(items, -1,  0, new ItemStack(disability.ritual[0]));
        }

        setItemRelativeToCenter(items,  1,  1, new ItemStack(disability.ritual[1]));
        setItemRelativeToCenter(items,  1, -1, new ItemStack(disability.ritual[1]));
        setItemRelativeToCenter(items, -1,  1, new ItemStack(disability.ritual[1]));
        setItemRelativeToCenter(items, -1, -1, new ItemStack(disability.ritual[1]));

        setItemRelativeToCenter(items,  0,  2, new ItemStack(disability.ritual[2]));
        setItemRelativeToCenter(items,  0, -2, new ItemStack(disability.ritual[2]));
        setItemRelativeToCenter(items,  2,  0, new ItemStack(disability.ritual[2]));
        setItemRelativeToCenter(items, -2,   0, new ItemStack(disability.ritual[2]));

        setItemRelativeToCenter(items, -1,  2, new ItemStack(disability.ritual[3]));
        setItemRelativeToCenter(items, -2,  1, new ItemStack(disability.ritual[3]));
        setItemRelativeToCenter(items,  1, -2, new ItemStack(disability.ritual[3]));
        setItemRelativeToCenter(items,  2, -1, new ItemStack(disability.ritual[3]));

        setItemRelativeToCenter(items, -1, -2, new ItemStack(disability.ritual[3]));
        setItemRelativeToCenter(items, -2, -1, new ItemStack(disability.ritual[3]));
        setItemRelativeToCenter(items,  1,  2, new ItemStack(disability.ritual[3]));
        setItemRelativeToCenter(items,  2,  1, new ItemStack(disability.ritual[3]));

        setItemRelativeToCenter(items,  2,  2, new ItemStack(disability.ritual[4]));
        setItemRelativeToCenter(items,  2, -2, new ItemStack(disability.ritual[4]));
        setItemRelativeToCenter(items, -2,  2, new ItemStack(disability.ritual[4]));
        setItemRelativeToCenter(items, -2, -2, new ItemStack(disability.ritual[4]));
    }

    public static void setItemRelativeToCenter(ItemStack[] items, int x, int y, ItemStack item) {
        int rx = x + 4;
        int ry = y + 3;
        items[rx + ry*9] = item;
    }
}