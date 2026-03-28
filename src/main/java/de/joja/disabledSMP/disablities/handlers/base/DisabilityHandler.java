package de.joja.disabledSMP.disablities.handlers.base;

import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.dismenu.menus.Menu;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
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

    public Menu specifcInfoMenu = null;

    public Material[] ritual = {Material.ANCIENT_DEBRIS, Material.DIAMOND_BLOCK, Material.HAY_BLOCK, Material.PUMPKIN, Material.MELON};

    public void initIcons() {
        icon = Key.key("dissmp", "disicons/" + enDataName);
        grayIcon = Key.key("dissmp", "gray_disicons/gray_" + enDataName);
        cureIcon = Key.key("dissmp", "cures/" + enDataName);
        grayCureIcon = Key.key("dissmp", "gray_cures/gray_" + enDataName);
    }

    protected static String toDataName(String name) {
        return name.toLowerCase()
                .replace(' ', '_')
                .replace('-', '_');
    }



    public static boolean hasDis(Player player, Disability dis) {
        return plugin.disabilityMap.get(player.getUniqueId()).contains(dis);
    }

    public void addToPlayer(Player player) {}
    public void removeFromPlayer(Player player) {}

}

