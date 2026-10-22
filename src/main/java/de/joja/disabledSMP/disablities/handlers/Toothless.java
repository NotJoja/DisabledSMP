package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.List;

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

        // Allow soups and drinkables (potions, milk)
        boolean allowed = item == Material.MUSHROOM_STEW
                || item == Material.BEETROOT_SOUP
                || item == Material.RABBIT_STEW
                || item == Material.SUSPICIOUS_STEW
                || item == Material.POTION
                || item == Material.MILK_BUCKET;

        if (!allowed) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("You can't eat that! Only soups and drinks are allowed for you.");
        }
    }
}
