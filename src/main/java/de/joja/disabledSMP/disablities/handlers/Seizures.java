package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.utils.Utils.random;

public class Seizures extends DisabilityHandler {

    private static final int TIMER_PERIOD_TICKS = 2;

    // time between two seizures: uniformly 15-25 minutes (average 20)
    private static final int MIN_COOLDOWN_TICKS = 15 * 60 * 20;
    private static final int MAX_COOLDOWN_TICKS = 25 * 60 * 20;

    // ticks left until the next seizure starts
    private final HashMap<UUID, Integer> cooldownMap = new HashMap<>();
    // ticks left of the current seizure (0 = no seizure right now)
    private final HashMap<UUID, Integer> seizureTimeMap = new HashMap<>();

    public Seizures() {
        this.enName = "Seizures";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You sometimes get a seizure,",
                "that makes you move randomly"
        );

        this.deName = "Krampfanfälle";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du bekommst manchmal einen Krampfanfall,",
                "bei denen du dich zufällig bewegst"
        );

        initIcons();

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {

                if (!hasDis(player, Disability.SEIZURES))
                    continue;

                UUID uuid = player.getUniqueId();
                int seizureTime = seizureTimeMap.getOrDefault(uuid, 0);

                if (seizureTime <= 0) {
                    // no seizure: count down to the next one
                    int cooldown = cooldownMap.computeIfAbsent(uuid, k -> randomCooldown()) - TIMER_PERIOD_TICKS;
                    if (cooldown > 0) {
                        cooldownMap.put(uuid, cooldown);
                        continue;
                    }

                    // start a seizure
                    seizureTimeMap.put(uuid, random.nextInt(100, 400));
                    cooldownMap.put(uuid, randomCooldown());
                    player.sendMessage("§c§lSEIZURE! §cYou move uncontrolled!");
                    continue;
                }

                double walkFactor = 0.2;
                if (!player.isOnGround())
                    walkFactor = 0.1;

                double vx = (Math.random() - 0.5) * walkFactor;
                double vy = 0;
                double vz = (Math.random() - 0.5) * walkFactor;

                if (player.isOnGround() && Math.random() < 0.08)
                    vy = player.getAttribute(Attribute.JUMP_STRENGTH).getBaseValue();

                Vector velocity = player.getVelocity();
                velocity.add(new Vector(vx, vy, vz));

                player.setVelocity(velocity);

                seizureTimeMap.put(uuid, seizureTime - TIMER_PERIOD_TICKS);
            }
        }, 20L, TIMER_PERIOD_TICKS);

    }

    private static int randomCooldown() {
        return random.nextInt(MIN_COOLDOWN_TICKS, MAX_COOLDOWN_TICKS + 1);
    }

    @Override
    public void addToPlayer(Player player) {
        seizureTimeMap.put(player.getUniqueId(), 0);
        cooldownMap.put(player.getUniqueId(), randomCooldown());
    }

    @Override
    public void removeFromPlayer(Player player) {
        seizureTimeMap.remove(player.getUniqueId());
        cooldownMap.remove(player.getUniqueId());
    }
}
