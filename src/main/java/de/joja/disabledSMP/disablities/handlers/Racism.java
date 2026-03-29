package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public class Racism extends DisabilityHandler {

    public static final Set<Material> DARK_MATERIALS = new HashSet<>(Set.of(Material.BEDROCK, Material.RESPAWN_ANCHOR, Material.DRAGON_EGG, Material.DRAGON_HEAD));

    static {
        for (Material mat : Material.values()) {
            String name = mat.name();

            if (name.contains("DARK") ||
                    name.contains("BLACK") ||
                    name.contains("DEEPSLATE") ||
                    name.contains("SCULK") ||
                    name.contains("OBSIDIAN") ||
                    name.contains("COAL") ||
                    name.contains("BASALT") ||
                    name.contains("NETHERITE") ||
                    name.contains("WITHER")) {

                DARK_MATERIALS.add(mat);
            }
        }
    }

    private final Map<UUID, Long> damageCooldown = new HashMap<>();

    public Racism() {
        this.enName = "Racism";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "Touching black blocks or items",
                "hurts you"
        );

        this.deName = "Rassismus";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Schwarze Blöcke oder Items",
                "zu berühren schadet dir"
        );

        initIcons();

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!hasDis(player, Disability.RACISM))
                    continue;

                checkInventory(player);
            }
        }, 20L, 20L);
    }

    public void checkInventory(Player player) {

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;

            if (DARK_MATERIALS.contains(item.getType())) {
                player.damage(2.0, DamageSource.builder(DamageType.MAGIC).build());
                return;
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (!hasDis(player, Disability.RACISM)) return;

        Location loc = player.getLocation();

        Block block = loc.getBlock();
        Block below = loc.clone().subtract(0, 1, 0).getBlock();

        if (DARK_MATERIALS.contains(block.getType()) ||
                DARK_MATERIALS.contains(below.getType())) {

            if (!canDamage(player)) return;

            player.damage(3.0);
            markDamage(player);
        }
    }

    private boolean canDamage(Player player) {
        long now = System.currentTimeMillis();

        if (!damageCooldown.containsKey(player.getUniqueId())) return true;

        return now - damageCooldown.get(player.getUniqueId()) > 1000;
    }

    private void markDamage(Player player) {
        damageCooldown.put(player.getUniqueId(), System.currentTimeMillis());
    }

}
