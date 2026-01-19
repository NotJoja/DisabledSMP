package de.joja.disabledSMP.storage;

import de.joja.disabledSMP.disablities.Disability;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static de.joja.disabledSMP.disablities.Disability.DISABILITIES_TOTAL_AMOUNT;

public abstract class YamlDisabilityStorage {

    public static List<Integer> loadDisabilityIDs(JavaPlugin plugin, UUID uuid) {
        File file = new File(plugin.getDataFolder(), "disabilities/" + uuid + ".yml");
        if (!file.exists()) return null;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        return (ArrayList<Integer>) cfg.get("disability_ids");
    }

    public static List<Disability> loadDisabilities(JavaPlugin plugin, UUID uuid) {
        List<Integer> disabilityIDs = loadDisabilityIDs(plugin, uuid);
        if (disabilityIDs == null)
            return null;

        List<Disability> disabilities = new ArrayList<>(DISABILITIES_TOTAL_AMOUNT);
        for (int i = 0; i < disabilityIDs.size(); i++)
            disabilities.add(Disability.get(disabilityIDs.get(i)));
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

    public static void saveDisabilities(JavaPlugin plugin, UUID uuid, Disability[] disabilities) {
        int[] disabilityIDs = new int[disabilities.length];
        for (int i = 0; i < disabilities.length; i++)
            disabilityIDs[i] = disabilities[i].ordinal();
        saveDisabilityIDs(plugin, uuid, disabilityIDs);
    }

}
