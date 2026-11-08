package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.advancements.AdvancementManager;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import de.joja.disabledSMP.listeners.DeathMessageListener;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.utils.Utils.random;

public class HeartAttacks extends DisabilityHandler {

    public HeartAttacks() {
        this.enName = "Heart Attacks";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You may randomly suffer sudden damage",
                "and get set to 1 hp"
        );

        this.deName = "Herzinfarkte";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du erleidest plötzlich zufällig Schaden",
                "und hast dann nur noch 1 hp"
        );

        initIcons();
    }

    private final Map<UUID, BukkitTask> tasks = new HashMap<>();

    @Override
    public void addToPlayer(final Player player) {

        // never run more than one timer per player
        cancelTask(player.getUniqueId());

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            if (!player.isOnline() || !hasDis(player, Disability.HEART_ATTACKS))
                return;

            // ~1 trigger every 30 minutes on average
            boolean randomChance = random.nextInt(1200) == 0;

            if (randomChance && player.getHealth() > 1)
                heartAttack(player);

        }, 0L, 20L);

        tasks.put(player.getUniqueId(), task);
    }

    @Override
    public void removeFromPlayer(Player player) {
        cancelTask(player.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancelTask(event.getPlayer().getUniqueId());
    }

    private void cancelTask(UUID uuid) {
        BukkitTask old = tasks.remove(uuid);
        if (old != null)
            old.cancel();
    }

    private static void heartAttack(Player player) {

        player.playSound(
                player.getLocation(),
                Sound.ENTITY_PLAYER_HURT_FREEZE,
                SoundCategory.PLAYERS,
                10f,
                0.2f
        );

        player.playSound(
                player.getLocation(),
                Sound.ENTITY_PLAYER_BREATH,
                SoundCategory.PLAYERS,
                5f,
                1f
        );

        String locale = player.getLocale();
        if (locale.startsWith("de"))
            player.sendMessage("§c§lHEART ATTACK! §c");
        else
            player.sendMessage("§c§lHERZINFARKT! §c");

        // always drops you to exactly half a heart (armor, resistance etc. can't reduce it, and you can't die from it)
        player.playHurtAnimation(0f);
        player.setHealth(1.0);
        DeathMessageListener.markHeartAttack(player);

        // Survived if still alive a few seconds later
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && !player.isDead())
                plugin.advancementManager.grant(player, AdvancementManager.STILL_STANDING);
        }, 60L);
    }

}
