package de.joja.disabledSMP.dismenu.menus;

import de.joja.disabledSMP.disablities.CureManager;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.dismenu.clickables.DisabilityClickable;
import de.joja.disabledSMP.dismenu.clickables.MenuClickable;
import de.joja.disabledSMP.dismenu.clickables.PlayerDisabilityExecutable;
import de.joja.disabledSMP.utils.ItemUtils;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.utils.Icons.*;

public class SpecDisMenu extends Menu {

    public final Disability disability;

    public SpecDisMenu(UUID uuid, Disability disability) {
        super(uuid);
        this.size = 9;
        this.disability = disability;
        initContents(size);
        createContents();
    }

    @Override
    public Inventory createMenuInv() {
        return createMenuInvHelper(disability.enName + " Menu", disability.deName + " Menü");
    }

    @Override
    protected void createContents() {

        createMenuOption(0, "Go Back", "Zurück", GO_BACK_ICON, SpecDisMenu::goBackExe);

        MenuClickable withdrawCure = new DisabilityClickable(disability, SpecDisMenu::withdrawCureExe);
        if (plugin.disabilityMap.get(uuid).contains(disability))
            createMenuOption(2,
                    "Withdraw Cure",
                    "Heilmittel Extrahieren",
                    List.of("Only possible if you don't have that Disability!"),
                    List.of("Nur möglich wenn du diese Disability nicht hast!"),
                    disability.grayCureIcon, withdrawCure);
        else
            createMenuOption(2, "Withdraw Cure", "Heilmittel Extrahieren", disability.cureIcon, withdrawCure);

        MenuClickable extraDisInfo = new DisabilityClickable(disability, SpecDisMenu::extraDisInfoExe);
        if (disability.specificInfoMenu == null)
            createMenuOption(4, "No Extra Disability Info", "Keine Extra Disability Info", GRAY_EXTRA_INFO_ICON, extraDisInfo);
        else
            createMenuOption(4, "Extra Disability Info", "Extra Disability Info", EXTRA_INFO_ICON, extraDisInfo);

        DisabilityClickable cureRitual = new DisabilityClickable(disability, SpecDisMenu::cureRitualExe);
        createMenuOption(6, "Cure Disability Ritual", "Disability Heilmittel Ritual", CURE_RITUAL_ICON, cureRitual);

        PlayerDisabilityExecutable addRitualExe = (player, disability) ->
                player.openInventory(new RitualMenu(player.getUniqueId(), disability, false).createMenuInv());
        DisabilityClickable addRitual = new DisabilityClickable(disability, addRitualExe);
        createMenuOption(8, "Add Disability Ritual", "Disability hinzufügen Ritual", ADD_RITUAL_ICON, addRitual);
    }

    private void createMenuOption(int i, String enName, String deName, Key icon, MenuClickable clickable) {
        createMenuOption(i, enName, deName, null, null, icon, clickable);
    }

    private void createMenuOption(int i, String enName, String deName, List<String> enDescription, List<String> deDescription, Key icon, MenuClickable clickable) {
        ItemStack item = new ItemStack(Material.IRON_NUGGET);
        ItemUtils.configureItem(item, icon, enName, deName, enDescription, deDescription, 0xF0F0FF, true);
        items[i] = item;
        clickables[i] = clickable;
    }

    private static void goBackExe(Player player) {
        player.openInventory(new MainDisMenu(player.getUniqueId()).createMenuInv());
    }

    private static void withdrawCureExe(Player player, Disability disability) {
        List<Disability> playerDisabilities = plugin.disabilityMap.get(player.getUniqueId());
        if (!playerDisabilities.contains(disability)) {
            player.give(CureManager.createCureItem(disability));
            playerDisabilities.add(disability);
            disability.handler.addToPlayer(player);
            player.closeInventory();
        }
    }

    private static void cureRitualExe(Player player, Disability disability) {
        player.openInventory(new RitualMenu(player.getUniqueId(), disability, true).createMenuInv());
    }



    private static void extraDisInfoExe(Player player, Disability disability) {
        if (disability.specificInfoMenu == null)
            return;
        player.openInventory(disability.specificInfoMenu.createMenuInv());
    }

}
