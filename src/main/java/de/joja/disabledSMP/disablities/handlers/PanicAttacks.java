package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import java.util.List;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public class PanicAttacks extends DisabilityHandler {

    public PanicAttacks() {
        this.enName = "Panic Attacks";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "If too many players or mobs are nearby,",
                "items can fall out of your inventory",
                "and you will get blindness and speed"
        );

        this.deName = "Panikattacken";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Wenn zu viele Spieler oder Mobs in deiner Nähe",
                "sind, können Items aus deinem Inventar fallen",
                "und du wirst blind und schneller"
        );

        initIcons();

        Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::checkPlayers,
                20L,
                100L
        );
    }
    private final Random random = new Random();

    private static final double DETECTION_RADIUS = 10.0;

    private static final double MOB_CHANCE = 0.005;
    private static final double MAX_CHANCE = 0.9;

    private static final int ATTACK_DURATION = 20;
    private static final double DROP_CHANCE = 0.1;

    private void checkPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {

            if (!hasDis(player, Disability.PANIC_ATTACKS))
                return;

            int nearbyPlayers = 0;
            int nearbyMobs = 0;

            for (Entity entity : player.getNearbyEntities(DETECTION_RADIUS, DETECTION_RADIUS, DETECTION_RADIUS)) {
                if (entity instanceof Player) {
                    nearbyPlayers++;
                } else if (entity instanceof Mob) {
                    nearbyMobs++;
                }
            }

            double playerChance = ((nearbyPlayers * 0.4)*(nearbyPlayers * 0.4) / 10);
            double chance = playerChance + nearbyMobs * MOB_CHANCE;
            chance = Math.min(chance, MAX_CHANCE);

            if (random.nextDouble() < chance)
                startPanicAttack(player, nearbyPlayers, nearbyMobs);
        }
    }

    private void startPanicAttack(Player player, int nearbyPlayers, int nearbyMobs) {
        if (!hasDis(player, Disability.PANIC_ATTACKS))
            return;

        player.sendMessage("§c§lPANIC ATTACK! §cYou are overwhelmed by your surroundings!");

        player.addPotionEffect(
                new PotionEffect(PotionEffectType.SPEED, ATTACK_DURATION * 20, 1, false, true, true)
        );

        new BukkitRunnable() {

            private int elapsedSeconds = 0;

            @Override
            public void run() {

                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (elapsedSeconds >= ATTACK_DURATION) {
                    player.sendMessage("§aYou have calmed down. Your panic attack is over.");
                    cancel();
                    return;
                }

                dropRandomThing(player);

                elapsedSeconds++;
            }

        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void dropRandomThing(Player player) {
        Random random = new Random();

        if (random.nextDouble() < 0.05) {
            ItemStack heldItem = player.getInventory().getItemInMainHand();

            if (heldItem.getType() != Material.AIR) {
                player.getWorld().dropItemNaturally(player.getLocation(), heldItem.clone());
                player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                player.sendMessage("§cYou panic and drop the item in your hand!");
            }
        }

        if (random.nextDouble() < 0.03) {
            ItemStack[] armor = player.getInventory().getArmorContents();
            List<Integer> equippedSlots = new ArrayList<>();

            for (int i = 0; i < armor.length; i++)
                if (armor[i] != null && armor[i].getType() != Material.AIR)
                    equippedSlots.add(i);

            if (!equippedSlots.isEmpty()) {

                int slot = equippedSlots.get(random.nextInt(equippedSlots.size()));

                ItemStack armorPiece = armor[slot].clone();
                armor[slot] = new ItemStack(Material.AIR);
                player.getInventory().setArmorContents(armor);

                if (random.nextBoolean()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), armorPiece);
                    player.sendMessage("§cYou panic and drop a piece of armor!");
                } else {
                    HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(armorPiece);
                    for (ItemStack item : leftover.values())
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                    player.sendMessage("§cYou panic and take off a piece of armor!");
                }
            }
        }
    }

}
