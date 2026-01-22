package de.joja.disabledSMP.storage;

import de.joja.disabledSMP.disablities.Disability;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.disablities.Disability.DISABILITIES_TOTAL_AMOUNT;

public abstract class YamlDisabilityStorage {

    public static List<Integer> loadDisabilityIDs(UUID uuid) {
        File file = new File(plugin.getDataFolder(), "disabilities/" + uuid + ".yml");
        if (!file.exists()) return null;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        return cfg.getIntegerList("disability_ids");
    }

    public static List<Disability> loadDisabilities(UUID uuid) {
        List<Integer> disabilityIDs = loadDisabilityIDs(uuid);
        if (disabilityIDs == null)
            return null;

        List<Disability> disabilities = new ArrayList<>(DISABILITIES_TOTAL_AMOUNT);
        for (int i = 0; i < disabilityIDs.size(); i++)
            disabilities.add(Disability.get(disabilityIDs.get(i)));
        return disabilities;
    }

    public static int loadAllergy(UUID uuid) {
        File file = new File(plugin.getDataFolder(), "disabilities/" + uuid + ".yml");
        if (!file.exists()) return -1;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        return cfg.getInt("allergy");
    }



    public static void saveDisabilityIDs(UUID uuid, int[] disabilityIDs, int allergyID) {
        File file = new File(plugin.getDataFolder(), "disabilities/" + uuid + ".yml");
        file.getParentFile().mkdirs();

        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("disability_ids", disabilityIDs);
        cfg.set("allergy_id", allergyID);
        try {
            cfg.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveDisabilities(UUID uuid, Disability[] disabilities, int allergy) {
        int[] disabilityIDs = new int[disabilities.length];
        for (int i = 0; i < disabilities.length; i++)
            disabilityIDs[i] = disabilities[i].ordinal();
        saveDisabilityIDs(uuid, disabilityIDs, allergy);
    }



}
