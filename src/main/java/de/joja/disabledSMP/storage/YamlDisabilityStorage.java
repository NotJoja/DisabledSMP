package de.joja.disabledSMP.storage;

import de.joja.disabledSMP.disablities.Disability;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static de.joja.disabledSMP.disablities.Disability.DISABILITIES_TOTAL_AMOUNT;

public abstract class YamlDisabilityStorage {

    /** Stored inside the main world folder, so deleting the world also resets everyone's disabilities. */
    private static File file(UUID uuid) {
        return new File(Bukkit.getWorlds().get(0).getWorldFolder(), "disabledsmp/" + uuid + ".yml");
    }

    public static List<Integer> loadDisabilityIDs(UUID uuid) {
        File file = file(uuid);
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
        File file = file(uuid);
        if (!file.exists()) return -1;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        return cfg.getInt("allergy_id", -1);
    }



    /** The colorblind mode chosen in /dis menu (the name of the mode), or null if none was chosen. */
    public static String loadColorblindMode(UUID uuid) {
        File file = file(uuid);
        if (!file.exists()) return null;

        return YamlConfiguration.loadConfiguration(file).getString("colorblind_mode");
    }

    public static void saveColorblindMode(UUID uuid, String mode) {
        File file = file(uuid);
        file.getParentFile().mkdirs();

        YamlConfiguration cfg = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();
        cfg.set("colorblind_mode", mode);
        try {
            cfg.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveDisabilityIDs(UUID uuid, int[] disabilityIDs) {
        File file = file(uuid);
        file.getParentFile().mkdirs();

        YamlConfiguration cfg;
        if (file.exists())
            cfg = YamlConfiguration.loadConfiguration(file);
        else
           cfg = new YamlConfiguration();

        cfg.set("disability_ids", disabilityIDs);
        try {
            cfg.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveDisabilities(UUID uuid, Disability[] disabilities) {
        int[] disabilityIDs = new int[disabilities.length];
        for (int i = 0; i < disabilities.length; i++)
            disabilityIDs[i] = disabilities[i].ordinal();
        saveDisabilityIDs(uuid, disabilityIDs);
    }

    public static void saveAllergy(UUID uuid, int allergyID) {
        File file = file(uuid);
        file.getParentFile().mkdirs();

        YamlConfiguration cfg;
        if (file.exists())
            cfg = YamlConfiguration.loadConfiguration(file);
        else
            cfg = new YamlConfiguration();

        cfg.set("allergy_id", allergyID);
        try {
            cfg.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
