package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

import java.util.List;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public class PanicAttacks extends DisabilityHandler {

    public PanicAttacks() {
        this.enName = "Panic Attacks";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "If too many players or mobs are nearby,",
                "you move and shake randomly"
        );

        this.deName = "Panikattacken";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Wenn zu viele Spieler oder Mobs in deiner Nähe sind,",
                "fängst du an dich zufällig zu bewegen und zu zittern"
        );

        initIcons();

        Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::checkPanicAttack,
                20L,
                100L
        );
        Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> {
                    for (Player player : plugin.getServer().getOnlinePlayers())
                        updatePanicAttack(player);
                },
                20L,
                UPDATE_PANIC_ATTACK_TICKS
        );
    }

    private final HashMap<UUID, Integer> panicAttacksTimeMap = new HashMap<>();

    private final Random random = new Random();

    private static final int UPDATE_PANIC_ATTACK_TICKS = 2;

    private static final double DETECTION_RADIUS = 10.0;

    private static final double MOB_CHANCE = 0.005;
    private static final double MAX_CHANCE = 0.9;

    private void checkPanicAttack() {
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

            if (random.nextDouble() < chance) {
                String locale = player.getLocale();
                if (locale.startsWith("de"))
                    player.sendMessage("§c§lPANIC ATTACK! §cYou move and shake randomly!");
                else
                    player.sendMessage("§c§lPANIK ATTACKE! §cDu bewegst und zitterst zufällig!");
                panicAttacksTimeMap.put(player.getUniqueId(), random.nextInt(10*20, 30*20));
            }
        }
    }

    private void updatePanicAttack(Player player) {

        UUID uuid = player.getUniqueId();
        int seizureTime = panicAttacksTimeMap.getOrDefault(uuid, 0);

        if (seizureTime <= 0)
            return;

        double walkFactor = 0.2;
        if (!player.isOnGround())
            walkFactor = 0.1;

        double vx = (Math.random() - 0.5) * walkFactor;
        double vy = 0;
        double vz = (Math.random() - 0.5) * walkFactor;

        if (player.isOnGround() && Math.random() < 0.08)
            vy = player.getAttribute(Attribute.JUMP_STRENGTH).getBaseValue();

        org.bukkit.util.Vector velocity = player.getVelocity();
        velocity.add(new Vector(vx, vy, vz));

        player.setVelocity(velocity);
        panicAttacksTimeMap.put(uuid, seizureTime - UPDATE_PANIC_ATTACK_TICKS);
    }

}
