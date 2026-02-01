package de.joja.disabledSMP.disablities.handlers.base;

import de.joja.disabledSMP.dismenu.menus.Menu;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;

import java.util.EventListener;
import java.util.List;

public abstract class DisabilityHandler implements EventListener {

    public String enName;
    public String enDataName;
    public List<String> enDescription;

    public String deName;
    public String deDataName;
    public List<String> deDescription;

    public Key icon;
    public Key grayIcon;
    public Key cureIcon;

    public Menu specifcInfoMenu = null;

    public Material[] ritual = {Material.ANCIENT_DEBRIS, Material.DIAMOND_BLOCK, Material.HAY_BLOCK, Material.PUMPKIN, Material.MELON};

    public void initIcons() {
        icon = Key.key("dissmp", "disicons/" + enDataName);
        grayIcon = Key.key("dissmp", "gray_disicons/gray_" + enDataName);
        cureIcon = Key.key("dissmp", "cure_" + enDataName);
    }

    public static String toDataName(String name) {
        return name.toLowerCase()
                .replace(' ', '_')
                .replace('-', '_');
    }
}

