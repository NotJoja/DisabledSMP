package de.joja.disabledSMP.listeners;

import de.joja.disabledSMP.advancements.AdvancementManager;
import de.joja.disabledSMP.disablities.CureManager;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.utils.DConfig;
import de.joja.disabledSMP.utils.Lang;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public class CureListener implements Listener {

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

        // the server has a minimum amount of disabilities: the cure can't be used below it
        var disabilities = plugin.disabilityMap.get(player.getUniqueId());
        if (disabilities == null)
            return;
        if (DConfig.MIN_DISABILITIES >= 0 && disabilities.size() <= DConfig.MIN_DISABILITIES) {
            event.setCancelled(true);
            player.sendMessage(Lang.of(player).pick(
                    "You can't be cured, you need at least " + DConfig.MIN_DISABILITIES + " disabilities!",
                    "Du kannst nicht geheilt werden, du brauchst mindestens " + DConfig.MIN_DISABILITIES + " Disabilities!"));
            return;
        }

        int cureDisabilityID = pdc.get(cureKey, PersistentDataType.INTEGER);
        if (cureDisabilityID < 0 || cureDisabilityID >= Disability.DISABILITIES_TOTAL_AMOUNT)
            return;
        Disability dis = Disability.get(cureDisabilityID);
        disabilities.remove(dis);
        dis.handler.removeFromPlayer(player);
        plugin.saveDisabilities(player.getUniqueId());
        plugin.advancementManager.grant(player, AdvancementManager.YUMMY);
    }

    /** Cures dropped on the ground survive fire, lava and explosions. */
    @EventHandler
    public void onItemDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Item item))
            return;

        switch (event.getCause()) {
            case FIRE, FIRE_TICK, LAVA, HOT_FLOOR, BLOCK_EXPLOSION, ENTITY_EXPLOSION -> {
            }
            default -> {
                return;
            }
        }

        ItemStack stack = item.getItemStack();
        if (stack.getType() == Material.POTATO && stack.hasItemMeta()
                && stack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "cure"), PersistentDataType.INTEGER))
            event.setCancelled(true);
    }



}
