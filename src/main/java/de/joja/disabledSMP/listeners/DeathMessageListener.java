package de.joja.disabledSMP.listeners;

import de.joja.disabledSMP.utils.Lang;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom death messages for deaths caused by a disability. Everyone gets the message in their own language.
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

    private static final Map<UUID, Long> nailsDamage = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> toothlessFood = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> heartAttacks = new ConcurrentHashMap<>();

    public static void markToothlessFood(Player player) {
        toothlessFood.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public static void markHeartAttack(Player player) {
        heartAttacks.put(player.getUniqueId(), System.currentTimeMillis());
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

        long foodTime = food != null && now - food <= TOOTHLESS_WINDOW_MS ? food : -1;
        long heartTime = heart != null && now - heart <= HEART_ATTACK_WINDOW_MS ? heart : -1;
        long nailsTime = nails != null && now - nails <= NAILS_WINDOW_MS ? nails : -1;
        if (foodTime < 0 && heartTime < 0 && nailsTime < 0)
            return;

        // death messages are turned off (gamerule) or removed by another plugin
        if (event.deathMessage() == null)
            return;

        // the one that happened last is the cause
        String name = player.getName();
        String en;
        String de;
        if (nailsTime >= foodTime && nailsTime >= heartTime) {
            en = name + " got nailed";
            de = name + " wurde genagelt";
        } else if (foodTime >= heartTime) {
            en = name + " forgot to chew";
            de = name + " hat vergessen zu kauen";
        } else {
            en = name + "'s heart rage quit";
            de = name + "'s Herz ist geragequitted";
        }

        // sent to everyone separately (their own language), so the normal broadcast is switched off
        event.deathMessage(null);
        for (Player online : Bukkit.getOnlinePlayers())
            online.sendMessage(Component.text(Lang.of(online).pick(en, de)));
        Bukkit.getConsoleSender().sendMessage(Component.text(Lang.defaultLang().pick(en, de)));
    }
}
