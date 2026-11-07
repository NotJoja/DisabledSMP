package de.joja.disabledSMP.disablities.handlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import de.joja.disabledSMP.utils.DConfig;
import de.joja.disabledSMP.utils.Lang;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.utils.Utils.random;

/**
 * Every player with asthma has a "breath" value: it goes up while sprinting and down (slower) while not sprinting.
 * When it reaches the player's limit (somewhere between 20 and 40 seconds of sprinting) they get winded: Weakness and
 * Slowness (and no sprinting) until the value is back at 0, so short breaks don't reset it.
 */
public class Asthma extends DisabilityHandler {

    // all values are in "seconds of sprinting"
    private static final double MIN_LIMIT = Math.min(DConfig.ASTHMA_MIN_LIMIT_S, DConfig.ASTHMA_MAX_LIMIT_S);
    private static final double MAX_LIMIT = Math.max(DConfig.ASTHMA_MIN_LIMIT_S, DConfig.ASTHMA_MAX_LIMIT_S);

    /** Breath gained per second of sprinting. */
    private static final double SPRINT_GAIN = DConfig.ASTHMA_SPRINT_GAIN;
    /** Breath recovered per second of not sprinting (slower than the gain). */
    private static final double REST_RECOVERY = DConfig.ASTHMA_REST_RECOVERY;

    /** The food level the client is told about while winded (sprinting needs more than 6). */
    private static final int NO_SPRINT_FOOD_LEVEL = 6;

    private static final int TICK_INTERVAL = 5;
    private static final double SECONDS_PER_TICK = TICK_INTERVAL / 20.0;

    private static class Breath {
        double value = 0;
        double limit = nextLimit();
        volatile boolean winded = false;
    }

    private final Map<UUID, Breath> breath = new ConcurrentHashMap<>();

    public Asthma() {
        this.enName = "Asthma";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "If you sprint too long you run out",
                "of breath and get Weakness and",
                "Slowness until you have recovered"
        );

        this.deName = "Asthma";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Wenn du zu lange sprintest, geht dir",
                "die Luft aus und du bekommst Schwäche",
                "und Verlangsamung, bis du dich erholt hast"
        );

        initIcons();

        // While winded every health/food update the player receives says "6 food", so the client can't sprint. The
        // real food level on the server is untouched (eating still fills it up) and the bar never flickers, because
        // the packets are changed instead of followed by a second one.
        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.HIGH, PacketType.Play.Server.UPDATE_HEALTH) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        Breath b = breath.get(event.getPlayer().getUniqueId());
                        if (b != null && b.winded && DConfig.ASTHMA_BLOCK_SPRINT)
                            event.getPacket().getIntegers().write(0, NO_SPRINT_FOOD_LEVEL);
                    }
                });

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!hasDis(player, Disability.ASTHMA)) {
                    breath.remove(player.getUniqueId());
                    continue;
                }
                tick(player, breath.computeIfAbsent(player.getUniqueId(), id -> new Breath()));
            }
        }, TICK_INTERVAL, TICK_INTERVAL);
    }

    private static double nextLimit() {
        return MIN_LIMIT + random.nextDouble() * (MAX_LIMIT - MIN_LIMIT);
    }

    private void tick(Player player, Breath b) {
        boolean sprinting = player.isSprinting();

        if (!b.winded) {
            if (sprinting)
                b.value += SPRINT_GAIN * SECONDS_PER_TICK;
            else
                b.value = Math.max(0, b.value - REST_RECOVERY * SECONDS_PER_TICK);

            if (b.value >= b.limit) {
                b.winded = true;
                DConfig.debug(player.getName() + " is out of breath (limit " + Math.round(b.limit) + "s)");
                player.sendHealthUpdate();      // goes out as "6 food" now
                player.sendActionBar(Component.text(Lang.of(player).pick(
                        "You are out of breath!", "Dir geht die Luft aus!")));
            }
        } else {
            // recovering (sprinting isn't possible now): it's only over when the value is back at 0
            b.value = Math.max(0, b.value - REST_RECOVERY * SECONDS_PER_TICK);

            if (b.value <= 0) {
                b.winded = false;
                DConfig.debug(player.getName() + " can breathe again");
                b.limit = nextLimit();
                removeEffects(player);
                player.sendHealthUpdate();      // the real food level again
                player.sendActionBar(Component.text(Lang.of(player).pick(
                        "You can breathe again.", "Du kannst wieder atmen.")));
                return;
            }
        }

        if (b.winded) {
            applyEffects(player);
            if (DConfig.ASTHMA_BLOCK_SPRINT)
                player.setSprinting(false);
        }
    }

    private static void applyEffects(Player player) {
        int duration = TICK_INTERVAL * 4;      // refreshed all the time, runs out quickly if something goes wrong
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration, 0, true, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, 0, true, false, true));
    }

    private static void removeEffects(Player player) {
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
    }

    @Override
    public void removeFromPlayer(Player player) {
        if (breath.remove(player.getUniqueId()) != null) {
            removeEffects(player);
            player.sendHealthUpdate();
        }
    }

    /** Out of breath: no sprinting until recovered. */
    @EventHandler
    public void onToggleSprint(PlayerToggleSprintEvent event) {
        if (!event.isSprinting())
            return;

        Breath b = breath.get(event.getPlayer().getUniqueId());
        if (b != null && b.winded && DConfig.ASTHMA_BLOCK_SPRINT)
            event.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        breath.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        breath.remove(event.getPlayer().getUniqueId());
    }
}
