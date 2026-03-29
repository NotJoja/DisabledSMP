package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public class BalanceDisorder extends DisabilityHandler {

    public BalanceDisorder() {
        this.enName = "Balance Disorder";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You may randomly get pushed",
                "off block edges"
        );

        this.deName = "Gleichgewichtsstörung";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du wirst zufällig von",
                "Blockkanten geschubst"
        );

        initIcons();

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            for (Player player : plugin.getServer().getOnlinePlayers()) {

                if (!hasDis(player, Disability.BALANCE_DISORDER))
                    return;

                Location loc = player.getLocation();
                Block block = loc.clone().add(0, -1, 0).getBlock();

                if (!(block.isSolid() || player.isOnGround()))
                    return;

                // Directions to check
                Vector[] directions = new Vector[] {
                        new Vector(1, 0, 0),   // east
                        new Vector(-1, 0, 0),  // west
                        new Vector(0, 0, 1),   // south
                        new Vector(0, 0, -1)   // north
                };

                List<Vector> unsafeDirections = new ArrayList<>();

                for (Vector dir : directions) {

                    Block relative = block.getRelative(
                            dir.getBlockX(),
                            0,
                            dir.getBlockZ()
                    );

                    Block below1 = relative.getRelative(0, -1, 0);
                    Block below2 = relative.getRelative(0, -2, 0);
                    boolean ground = relative.getType().isSolid();
                    boolean groundBelow1 = below1.getType().isSolid();
                    boolean groundBelow2 = below2.getType().isSolid();

                    if (!ground && !groundBelow1 && !groundBelow2)
                        unsafeDirections.add(dir);
                }

                if (unsafeDirections.isEmpty())
                    return;

                if (Math.random() > unsafeDirections.size()*0.01)
                    return;

                // Pick random unsafe direction
                Vector pushDir = unsafeDirections.get(
                        (int) (Math.random() * unsafeDirections.size())
                );

                // Apply push
                Vector velocity = pushDir.clone().normalize().multiply(0.4);

                if (player.isSneaking())
                    velocity.setY(0.4);
                else
                    velocity.setY(0.2);

                player.setVelocity(velocity);
            }

        }, 1L, 5L);
    }
}
