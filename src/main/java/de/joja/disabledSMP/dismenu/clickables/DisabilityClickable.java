package de.joja.disabledSMP.dismenu.clickables;

import de.joja.disabledSMP.disablities.Disability;
import org.bukkit.entity.Player;

public class DisabilityClickable implements MenuClickable {

    public final Disability disability;
    private final PlayerDisabilityExecutable execute;

    public DisabilityClickable(Disability disability, PlayerDisabilityExecutable execute) {
        this.disability = disability;
        this.execute = execute;
    }

    @Override
    public void onClick(Player player) {
        execute.execute(player, disability);
    }
}
