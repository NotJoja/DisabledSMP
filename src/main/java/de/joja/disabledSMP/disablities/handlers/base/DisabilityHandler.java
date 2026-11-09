package de.joja.disabledSMP.disablities.handlers.base;

import de.joja.disabledSMP.disablities.Disability;
import io.papermc.paper.dialog.Dialog;
import org.bukkit.entity.Player;
import java.util.function.Function;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.List;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public abstract class DisabilityHandler implements Listener {

    public String enName;
    public String enDataName;
    public List<String> enDescription;

    public String deName;
    public String deDataName;
    public List<String> deDescription;

    public Key icon;
    public Key grayIcon;
    public Key cureIcon;
    public Key grayCureIcon;

    public Function<Player, Dialog> specificInfoDialog = null; // optional extra info page for the /dis menu

    public void initIcons() {
        icon = Key.key("dismp", "disicons/" + enDataName);
        grayIcon = Key.key("dismp", "gray_disicons/gray_" + enDataName);
        cureIcon = Key.key("dismp", "cures/" + enDataName);
        grayCureIcon = Key.key("dismp", "gray_cures/gray_" + enDataName);
    }

    protected static String toDataName(String name) {
        return name.toLowerCase()
                .replace(' ', '_')
                .replace('-', '_');
    }



    public static boolean hasDis(Player player, Disability dis) {
        List<Disability> disabilities = plugin.disabilityMap.get(player.getUniqueId());
        // a disability that is turned off in the config has no effect (the player keeps it saved)
        return disabilities != null && dis.isEnabled() && disabilities.contains(dis);
    }

    public void addToPlayer(Player player) {}
    public void removeFromPlayer(Player player) {}

}

