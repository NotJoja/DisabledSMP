package de.joja.disabledSMP.dismenu.clickables;

import de.joja.disabledSMP.disablities.CureManager;
import de.joja.disabledSMP.disablities.Disability;
import org.bukkit.entity.Player;

public class WithdrawCureClickable implements MenuClickable {

    public Disability disability;

    public WithdrawCureClickable(Disability disability) {
        this.disability = disability;
    }

    @Override
    public void onClick(Player player) {
        player.give(CureManager.createCureItem(disability));
    }
}
