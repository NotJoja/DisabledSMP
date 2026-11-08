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

    private static final Map<UUID, Long> toothlessFood = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> heartAttacks = new ConcurrentHashMap<>();

    public static void markToothlessFood(Player player) {
        toothlessFood.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public static void markHeartAttack(Player player) {
        heartAttacks.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        long now = System.currentTimeMillis();

        Long food = toothlessFood.remove(player.getUniqueId());
        Long heart = heartAttacks.remove(player.getUniqueId());

        boolean foodDeath = food != null && now - food <= TOOTHLESS_WINDOW_MS;
        boolean heartDeath = heart != null && now - heart <= HEART_ATTACK_WINDOW_MS;
        if (!foodDeath && !heartDeath)
            return;

        // death messages are turned off (gamerule) or removed by another plugin
        if (event.deathMessage() == null)
            return;

        // the one that happened last is the cause
        boolean byFood = foodDeath && (!heartDeath || food >= heart);

        String name = player.getName();
        String en = byFood ? name + " forgot to chew" : name + "'s heart rage quit";
        String de = byFood ? name + " hat vergessen zu kauen" : name + "'s Herz ist geragequitted";

        // sent to everyone separately (their own language), so the normal broadcast is switched off
        event.deathMessage(null);
        for (Player online : Bukkit.getOnlinePlayers())
            online.sendMessage(Component.text(Lang.of(online).pick(en, de)));
        Bukkit.getConsoleSender().sendMessage(Component.text(Lang.defaultLang().pick(en, de)));
    }
}
