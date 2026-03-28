package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.utils.Utils.random;

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
                    continue;

                if (!player.isOnGround())
                    continue;

                Location loc = player.getLocation();

                double x = loc.getX() % 1;
                double z = loc.getZ() % 1;

                boolean nearEdge = x < 0.3 || x > 0.7 || z < 0.3 || z > 0.7;
                if (!nearEdge)
                    continue;

                if (random.nextDouble() > 0.05)
                    continue;

                Location below1 = loc.clone().add(0, -1, 0);
                Location below2 = loc.clone().add(0, -2, 0);
                if (below1.getBlock().isSolid() && below2.getBlock().isSolid())
                    continue;

                Vector push = new Vector(x - 0.5, 0, z - 0.5).normalize();
                push.multiply(-0.4);
                push.setY(0.18);
                player.setVelocity(push);
            }

        }, 1L, 5L);
    }
}
