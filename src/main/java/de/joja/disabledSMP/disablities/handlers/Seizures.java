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

    HashMap<UUID, Integer> seizureTimeMap = new HashMap<>();

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

                int seizureTime = seizureTimeMap.get(player.getUniqueId());
                if (seizureTime <= -1) {
                    if (Math.random() < 0.0015)
                        seizureTimeMap.put(player.getUniqueId(), random.nextInt(100, 1000));
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

                seizureTimeMap.put(player.getUniqueId(), seizureTime - 2);
            }
        }, 20L, 2L);

    }

    @Override
    public void addToPlayer(Player player) {
        seizureTimeMap.put(player.getUniqueId(), -1);
    }
}
