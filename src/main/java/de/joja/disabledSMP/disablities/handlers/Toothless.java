package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import de.joja.disabledSMP.utils.DConfig;
import org.bukkit.Material;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.List;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public class Toothless extends DisabilityHandler {

    public Toothless() {
        this.enName = "Toothless";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You can only eat soft food",
                "or soup properly"
        );

        this.deName = "Zahnlos";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du kannst nur weiches Essen",
                "oder Suppen richtig essen"
        );

        initIcons();
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        if (!hasDis(event.getPlayer(), Disability.TOOTHLESS)) return;

        Material item = event.getItem().getType();

        boolean fluid = item == Material.MUSHROOM_STEW
                || item == Material.BEETROOT_SOUP
                || item == Material.RABBIT_STEW
                || item == Material.SUSPICIOUS_STEW
                || item == Material.POTION
                || item == Material.MILK_BUCKET;

        boolean softFood = item == Material.BREAD
                || item == Material.APPLE
                || item == Material.GOLDEN_APPLE
                || item == Material.ENCHANTED_GOLDEN_APPLE
                || item == Material.COOKIE
                || item == Material.CAKE
                || item == Material.MELON_SLICE
                || item == Material.SWEET_BERRIES
                || item == Material.GLOW_BERRIES
                || item == Material.BAKED_POTATO
                || item == Material.POISONOUS_POTATO
                || item == Material.PUMPKIN_PIE
                || item == Material.CARROT
                || item == Material.POTATO
                || item == Material.BEETROOT
                || item == Material.CHORUS_FRUIT
                || item == Material.DRIED_KELP;

        if (!fluid && !softFood) {
            event.setCancelled(true);
            if (DConfig.LANGUAGE.equals("de"))
                event.getPlayer().sendMessage("Du hast keine Zähne. Du kannst nur Suppen oder weiches Essen essen!");
            else if (DConfig.LANGUAGE.equals("en"))
                event.getPlayer().sendMessage("You have no teeth. You can only eat soups and soft food!");
            return;
        }

        if (softFood) {
            event.getPlayer().getScheduler().runDelayed(
                    plugin,
                    task -> {
                        if (event.getPlayer().isOnline())
                            event.getPlayer().damage(5, DamageSource.builder(DamageType.STARVE).build());

                        if (DConfig.LANGUAGE.equals("de"))
                            event.getPlayer().sendMessage("Du hast keine Zähne. Dieses Essen fügt dir Schaden hinzu!");
                        else if (DConfig.LANGUAGE.equals("en"))
                            event.getPlayer().sendMessage("You have no teeth. This food hurts you!");
                    },
                    null,
                    1L
            );
        }
    }

}
