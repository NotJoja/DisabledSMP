package de.joja.disabledSMP.disablities.handlers;

import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class Nearsighted extends DisabilityHandler {

    public Nearsighted() {
        this.enName = "Nearsighted";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You have a reduced view distance"
        );

        this.deName = "Kurzsichtigkeit";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du hast eine verringerte Sichtweite"
        );

        initIcons();
    }



    @Override
    public void addToPlayer(Player player) {
        player.setViewDistance(2);
        player.setSimulationDistance(2);
    }

    @Override
    public void removeFromPlayer(Player player) {
        player.setViewDistance(Bukkit.getViewDistance());
        player.setSimulationDistance(Bukkit.getSimulationDistance());
    }

}
