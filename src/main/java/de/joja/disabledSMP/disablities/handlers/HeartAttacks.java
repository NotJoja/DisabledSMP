package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.utils.Utils.random;

public class HeartAttacks extends DisabilityHandler {

    public HeartAttacks() {
        this.enName = "Heart Attacks";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You may randomly suffer sudden damage",
                "up to 8 hearts"
        );

        this.deName = "Herzinfarkte";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du erleidest zufällig Schaden",
                "bis zu 8 Herzen"
        );

        initIcons();
    }

    @Override
    public void addToPlayer(final Player player) {

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            if (!hasDis(player, Disability.HEART_ATTACKS))
                return;

            boolean randomChance = random.nextInt(300) == 0;
            if (randomChance && player.getHealth() >= 5) {

                double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getBaseValue();
                double damage = random.nextDouble(0.4, 0.8) * maxHealth;
                if (damage < player.getHealth()) {
                    heartAttack(player, damage);
                    return;
                }

                if (random.nextInt(4) == 0)
                    heartAttack(player, damage);
                else
                    heartAttack(player, player.getHealth() - 1);
            }

        }, 0L, 20L);
    }

    private static void heartAttack(Player player, double damage) {

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

        player.damage(damage);
    }

}
