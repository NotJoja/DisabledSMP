package de.joja.disabledSMP.listeners;

import de.joja.disabledSMP.disablities.CureManager;
import de.joja.disabledSMP.disablities.Disability;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public class CureListener implements Listener {

    @EventHandler
    public void onApple(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item.getType() == Material.APPLE) {
            ItemStack cureItem = CureManager.createCureItem(Disability.ALLERGY);
            player.getInventory().addItem(cureItem);
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item.getType() != Material.POTATO || !item.hasItemMeta())
            return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        NamespacedKey cureKey = new NamespacedKey(plugin, "cure");

        if (!pdc.has(cureKey, PersistentDataType.INTEGER))
            return;

        int cureDisabilityID = pdc.get(cureKey, PersistentDataType.INTEGER);
        System.out.println("curing disability id: " + cureDisabilityID);

        plugin.disabilityMap
                .get(player.getUniqueId())
                .remove(Disability.get(cureDisabilityID));
    }



}
