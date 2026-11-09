package de.joja.disabledSMP.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom death messages for deaths caused by a disability. Everyone sees the message in their own language (lang files of the resource pack).
 * The handlers note down when something happened with {@link #markToothlessFood} / {@link #markHeartAttack},
 * a death shortly afterwards uses the matching message.
 */
public class DeathMessageListener implements Listener {

    /** A death this soon after hurting yourself with food counts as dying from it. */
    private static final long TOOTHLESS_WINDOW_MS = 3_000;
    /** A death this soon after a heart attack counts as dying from it. */
    private static final long HEART_ATTACK_WINDOW_MS = 30_000;

    /** A death this soon after being hurt by nails counts as dying from them. */
    private static final long NAILS_WINDOW_MS = 3_000;

    /** A death this soon after an allergic reaction counts as dying from it. */
    private static final long ALLERGY_WINDOW_MS = 3_000;

    private static final Map<UUID, Long> allergyDamage = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> nailsDamage = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> toothlessFood = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> heartAttacks = new ConcurrentHashMap<>();

    public static void markToothlessFood(Player player) {
        toothlessFood.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public static void markHeartAttack(Player player) {
        heartAttacks.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /** Called right before an allergic reaction hurts a player. */
    public static void markAllergy(Player player) {
        allergyDamage.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /** Called right before the nails hurt a player. */
    public static void markNails(Player player) {
        nailsDamage.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        long now = System.currentTimeMillis();

        Long food = toothlessFood.remove(player.getUniqueId());
        Long heart = heartAttacks.remove(player.getUniqueId());
        Long nails = nailsDamage.remove(player.getUniqueId());
        Long allergy = allergyDamage.remove(player.getUniqueId());

        long foodTime = food != null && now - food <= TOOTHLESS_WINDOW_MS ? food : -1;
        long heartTime = heart != null && now - heart <= HEART_ATTACK_WINDOW_MS ? heart : -1;
        long nailsTime = nails != null && now - nails <= NAILS_WINDOW_MS ? nails : -1;
        long allergyTime = allergy != null && now - allergy <= ALLERGY_WINDOW_MS ? allergy : -1;
        if (foodTime < 0 && heartTime < 0 && nailsTime < 0 && allergyTime < 0)
            return;

        // death messages are turned off (gamerule) or removed by another plugin
        if (event.deathMessage() == null)
            return;

        // the one that happened last is the cause
        String key;
        String fallback;
        if (allergyTime >= nailsTime && allergyTime >= foodTime && allergyTime >= heartTime) {
            key = "death.dismp.allergy";
            fallback = "%s sneezed their last sneeze";
        } else if (nailsTime >= foodTime && nailsTime >= heartTime) {
            key = "death.dismp.nails";
            fallback = "%s got nailed";
        } else if (foodTime >= heartTime) {
            key = "death.dismp.toothless";
            fallback = "%s forgot to chew";
        } else {
            key = "death.dismp.heart_attack";
            fallback = "%s's heart rage quit";
        }

        // translated by the client with the lang files of the resource pack, so the normal death broadcast
        // does the job; the english fallback is used without the pack
        event.deathMessage(Component.translatable(key, fallback, player.name()));
    }
}
