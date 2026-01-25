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

    public Key icon = Key.key("", "");
    public Key grayIcon = Key.key("", "");

    public Key cureIcon = Key.key("", "");

    public Menu specifcInfoMenu = null;

    public Material[] cureDisRitual = {Material.GOLD_BLOCK, Material.HAY_BLOCK};
    public Material[] addDisRitual = {};

    public static String toDataName(String name) {
        return name.toLowerCase()
                .replace(' ', '_')
                .replace('-', '_');
    }
}

