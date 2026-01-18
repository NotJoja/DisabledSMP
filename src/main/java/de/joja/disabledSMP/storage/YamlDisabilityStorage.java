package de.joja.disabledSMP.storage;

import de.joja.disabledSMP.Disability;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public abstract class YamlDisabilityStorage {

    public static int[] loadDisabilityIDs(JavaPlugin plugin, UUID uuid) {
        File file = new File(plugin.getDataFolder(), "disabilities/" + uuid + ".yml");
        if (!file.exists()) return null;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        return (int[]) cfg.get("disability_ids");
    }

    public static Disability[] loadDisabilities(JavaPlugin plugin, UUID uuid) {
        int[] disabilityIDs = loadDisabilityIDs(plugin, uuid);
        if (disabilityIDs == null)
            return null;

        Disability[] disabilities = new Disability[disabilityIDs.length];
        for (int i = 0; i < disabilityIDs.length; i++)
            disabilities[i] = Disability.values()[disabilityIDs[i]];
        return disabilities;
    }



    public static void saveDisabilityIDs(JavaPlugin plugin, UUID uuid, int[] disabilityIDs) {
        File file = new File(plugin.getDataFolder(), "disabilities/" + uuid + ".yml");
        file.getParentFile().mkdirs();

        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("disability_ids", disabilityIDs);
        try {
            cfg.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
