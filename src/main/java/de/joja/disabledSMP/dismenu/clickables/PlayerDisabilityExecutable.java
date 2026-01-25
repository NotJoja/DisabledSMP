package de.joja.disabledSMP.dismenu.clickables;

import de.joja.disabledSMP.disablities.Disability;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface PlayerDisabilityExecutable {

    void execute(Player player, Disability disability);

}
