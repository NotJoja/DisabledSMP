package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public class ADHD extends DisabilityHandler {

    public static final double DAMAGE = 1.0;
    public static final int STAY_TIME_MS = 10_000;
    public static final int MOVE_RANGE = 8;

    Map<UUID, Location> lastLocation = new HashMap<>();
    Map<UUID, Long> lastMoveTime = new HashMap<>();

    public ADHD() {
        this.enName = "ADHD";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You can't stay for too",
                "long in the same area"
        );

        this.deName = "ADHS";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du kannst nicht zu lange",
                "am selben Ort bleiben"
        );

        initIcons();

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();

            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();

                if (!hasDis(player, Disability.ADHD))
                    continue;

                if (!lastMoveTime.containsKey(uuid))
                    continue;

                if (player.getLocation().distanceSquared(lastLocation.get(uuid)) > MOVE_RANGE)
                    continue;

                long timeStayed = now - lastMoveTime.get(uuid);

                if (timeStayed > STAY_TIME_MS) {
                    double newHealth = Math.max(0, player.getHealth() - DAMAGE);
                    player.setHealth(newHealth);
                }
            }
        }, 20L, 20L);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!hasDis(player, Disability.ADHD))
            return;

        Location location = event.getTo();
        if (lastLocation.containsKey(uuid) && location.distanceSquared(lastLocation.get(uuid)) <= MOVE_RANGE)
            return;

        lastLocation.put(uuid, location);
        lastMoveTime.put(uuid, System.currentTimeMillis());
    }
}
