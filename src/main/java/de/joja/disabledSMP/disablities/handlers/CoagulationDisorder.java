package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import de.joja.disabledSMP.utils.ItemUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public class CoagulationDisorder extends DisabilityHandler {

    public CoagulationDisorder() {
        this.enName = "Coagulation Disorder";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "If you get damaged, you won't regenerate,",
                "unless you use bandages"
        );

        this.deName = "Gerinnungsstörung";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du kannst dich nur regenerieren,",
                "wenn du Bandagen benutzt"
        );

        initIcons();
        Bukkit.getScheduler().runTaskLater(plugin, this::registerBandageRecipe, 20);
    }

    private final NamespacedKey bandageKey = new NamespacedKey("dismp", "bandage_key");
    private final Set<UUID> healingPlayers = new HashSet<>();

    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (!healingPlayers.contains(player.getUniqueId()))
            event.setCancelled(true);
    }

    // Detect bandage consumption
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();

        if (!meta.getPersistentDataContainer().has(
                bandageKey, PersistentDataType.BYTE)) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // A bandage is a milk bucket, so vanilla would hand back an empty bucket for every one of a stack.
        // Take one away ourselves instead.
        event.setCancelled(true);
        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack held = player.getInventory().getItem(event.getHand());
            held.setAmount(held.getAmount() - 1);
            player.getInventory().setItem(event.getHand(), held.getAmount() <= 0 ? null : held);
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.5f, 1f);

        healingPlayers.add(uuid);

        player.setFoodLevel(20);

        // Allow regeneration for 8 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            healingPlayers.remove(uuid);
        }, 20L * 10);
    }

    private void registerBandageRecipe() {
        ItemStack bandage = createBandage();
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey("dismp", "bandage_recipe"), bandage);
        recipe.shape(
                "#P#",
                "PRP",
                "#P#"
        );
        recipe.setIngredient('P', Material.PAPER);
        recipe.setIngredient('R', Material.RED_WOOL);
        Bukkit.addRecipe(recipe);
    }

    // Create bandage item
    public ItemStack createBandage() {
        ItemStack bandage = new ItemStack(Material.MILK_BUCKET);
        ItemMeta meta = bandage.getItemMeta();

        meta.setDisplayName("Bandage");
        meta.setItemModel(new NamespacedKey("dismp", "bandage"));

        meta.getPersistentDataContainer().set(bandageKey, PersistentDataType.BYTE, (byte) 1);
        bandage.setItemMeta(meta);
        bandage.setData(DataComponentTypes.MAX_STACK_SIZE, 16);

        return bandage;
    }
}