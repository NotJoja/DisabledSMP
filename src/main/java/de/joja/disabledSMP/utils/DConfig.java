package de.joja.disabledSMP.utils;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public abstract class DConfig {

    public static volatile String LANGUAGE;
    public static volatile String RESOURCE_PACK_URL;
    public static volatile String RESOURCE_PACK_SHA1;
    public static volatile String COLORBLIND_PACK_URL;
    public static volatile String COLORBLIND_PACK_SHA1;
    public static volatile String COLORBLIND_CLASSIC_PACK_URL;
    public static volatile String COLORBLIND_CLASSIC_PACK_SHA1;
    public static volatile int MAX_KILL_TIME_MS;

    // ---- housekeeping
    public static volatile boolean DEBUG;

    /** Prints to the console if "debug" is on in the config. */
    public static void debug(String message) {
        if (DEBUG)
            plugin.getLogger().info("[debug] " + message);
    }

    // ---- how many disabilities a player has (-1 = off / default)
    public static volatile int FIRST_JOIN_DISABILITIES;
    public static volatile int MIN_DISABILITIES;
    public static volatile int MAX_DISABILITIES;

    /** Data names (see "disabilities" in the config) of the disabilities that are turned off. */
    public static volatile java.util.Set<String> DISABLED_DISABILITIES = java.util.Set.of();

    // ---- Smoke gives disabilities
    public static volatile boolean SMOKE_DISABILITY_ENABLED;
    public static volatile double SMOKE_DISABILITY_SECONDS;
    public static volatile double SMOKE_DISABILITY_MIN_FACTOR;
    public static volatile int SMOKE_DISABILITY_COOLDOWN_S;

    // ---- Asthma
    public static volatile double ALLERGY_RADIUS;
    public static volatile double ASTHMA_MIN_LIMIT_S;
    public static volatile double ASTHMA_MAX_LIMIT_S;
    public static volatile double ASTHMA_SPRINT_GAIN;
    public static volatile double ASTHMA_REST_RECOVERY;
    public static volatile double ASTHMA_WINDED_RECOVERY;
    public static volatile boolean ASTHMA_BLOCK_SPRINT;

    // ---- Mentally Disabled
    public static volatile double MENTALLY_DISABLED_MISCRAFT_CHANCE;
    public static volatile boolean MENTALLY_DISABLED_CHAT_SHUFFLE;

    // ---- Schizophrenia
    public static volatile double SCHIZOPHRENIA_FAKE_MOB_CHANCE;
    public static volatile double SCHIZOPHRENIA_FAKE_MOB_CHANCE_NETHER;
    public static volatile int SCHIZOPHRENIA_HUNGRY_FOOD_LEVEL;
    public static volatile double SCHIZOPHRENIA_HUNGRY_MOB_CHANCE;
    public static volatile double SCHIZOPHRENIA_FAKE_ORE_CHANCE;
    public static volatile int SCHIZOPHRENIA_FAKE_ORE_RADIUS;
    public static volatile int SCHIZOPHRENIA_FAKE_ORE_MAX_AMOUNT;

    // ---- Overweight
    public static volatile double OVERWEIGHT_HUNGER_DRAIN;
    public static volatile double OVERWEIGHT_MOVEMENT_SPEED;
    public static volatile double OVERWEIGHT_JUMP_STRENGTH;
    public static volatile double OVERWEIGHT_EXPLOSION_MIN_FALL;

    // ---- Dwarfism
    public static volatile double DWARFISM_SCALE;
    public static volatile double DWARFISM_ENTITY_RANGE;
    public static volatile double DWARFISM_BLOCK_RANGE;
    public static volatile double DWARFISM_MAX_HEALTH;

    static {
        load();
    }

    /** Reads every value from the config (again). */
    private static void load() {
        SMOKE_DISABILITY_ENABLED = plugin.getConfig().getBoolean("smoke_disability.enabled", true);
        SMOKE_DISABILITY_SECONDS = plugin.getConfig().getDouble("smoke_disability.seconds_without_disabilities", 15);
        SMOKE_DISABILITY_MIN_FACTOR = plugin.getConfig().getDouble("smoke_disability.min_factor", 0.05);
        SMOKE_DISABILITY_COOLDOWN_S = plugin.getConfig().getInt("smoke_disability.cooldown_s", 600);
        LANGUAGE = plugin.getConfig().getString("language");
        RESOURCE_PACK_URL = plugin.getConfig().getString("resource_pack_url", "https://raw.githubusercontent.com/NotJoja/DismpTP/master/DisabledSMP/dismp.zip");
        RESOURCE_PACK_SHA1 = plugin.getConfig().getString("resource_pack_sha1", "auto");
        COLORBLIND_PACK_URL = plugin.getConfig().getString("colorblind_pack_url", "https://raw.githubusercontent.com/NotJoja/DismpTP/master/Colorblind/Colorblind.zip");
        COLORBLIND_PACK_SHA1 = plugin.getConfig().getString("colorblind_pack_sha1", "auto");
        COLORBLIND_CLASSIC_PACK_URL = plugin.getConfig().getString("colorblind_classic_pack_url", "https://raw.githubusercontent.com/NotJoja/DismpTP/master/ColorblindClassic/ColorblindClassic.zip");
        COLORBLIND_CLASSIC_PACK_SHA1 = plugin.getConfig().getString("colorblind_classic_pack_sha1", "auto");
        MAX_KILL_TIME_MS = plugin.getConfig().getInt("max_kill_time_s") * 1000;
        DEBUG = plugin.getConfig().getBoolean("debug", false);
        FIRST_JOIN_DISABILITIES = plugin.getConfig().getInt("first_join_disabilities", -1);
        MIN_DISABILITIES = plugin.getConfig().getInt("min_disabilities", -1);
        MAX_DISABILITIES = plugin.getConfig().getInt("max_disabilities", -1);
        java.util.Set<String> disabled = new java.util.HashSet<>();
        org.bukkit.configuration.ConfigurationSection enabledSection = plugin.getConfig().getConfigurationSection("disabilities");
        if (enabledSection != null)
            for (String name : enabledSection.getKeys(false))
                if (!enabledSection.getBoolean(name, true))
                    disabled.add(name.toLowerCase());
        DISABLED_DISABILITIES = java.util.Set.copyOf(disabled);
        ALLERGY_RADIUS = plugin.getConfig().getDouble("allergy.radius", 3);
        ASTHMA_MIN_LIMIT_S = plugin.getConfig().getDouble("asthma.min_limit_s", 20);
        ASTHMA_MAX_LIMIT_S = plugin.getConfig().getDouble("asthma.max_limit_s", 30);
        ASTHMA_SPRINT_GAIN = plugin.getConfig().getDouble("asthma.sprint_gain", 1.0);
        ASTHMA_REST_RECOVERY = plugin.getConfig().getDouble("asthma.rest_recovery", 0.5);
        ASTHMA_WINDED_RECOVERY = plugin.getConfig().getDouble("asthma.winded_recovery", 0.75);
        ASTHMA_BLOCK_SPRINT = plugin.getConfig().getBoolean("asthma.block_sprint", true);
        MENTALLY_DISABLED_MISCRAFT_CHANCE = plugin.getConfig().getDouble("mentally_disabled.miscraft_chance", 0.5);
        MENTALLY_DISABLED_CHAT_SHUFFLE = plugin.getConfig().getBoolean("mentally_disabled.chat_shuffle", true);
        SCHIZOPHRENIA_FAKE_MOB_CHANCE = plugin.getConfig().getDouble("schizophrenia.fake_mob_chance", 0.05);
        SCHIZOPHRENIA_FAKE_MOB_CHANCE_NETHER = plugin.getConfig().getDouble("schizophrenia.fake_mob_chance_nether", 0.10);
        SCHIZOPHRENIA_HUNGRY_FOOD_LEVEL = plugin.getConfig().getInt("schizophrenia.hungry_food_level", 6);
        SCHIZOPHRENIA_HUNGRY_MOB_CHANCE = plugin.getConfig().getDouble("schizophrenia.hungry_mob_chance", 0.7);
        SCHIZOPHRENIA_FAKE_ORE_CHANCE = plugin.getConfig().getDouble("schizophrenia.fake_ore_chance", 0.04);
        SCHIZOPHRENIA_FAKE_ORE_RADIUS = plugin.getConfig().getInt("schizophrenia.fake_ore_radius", 16);
        SCHIZOPHRENIA_FAKE_ORE_MAX_AMOUNT = plugin.getConfig().getInt("schizophrenia.fake_ore_max_amount", 2);
        OVERWEIGHT_HUNGER_DRAIN = plugin.getConfig().getDouble("overweight.hunger_drain", 0.03);
        OVERWEIGHT_MOVEMENT_SPEED = plugin.getConfig().getDouble("overweight.movement_speed", 0.08);
        OVERWEIGHT_JUMP_STRENGTH = plugin.getConfig().getDouble("overweight.jump_strength", 0.36);
        OVERWEIGHT_EXPLOSION_MIN_FALL = plugin.getConfig().getDouble("overweight.explosion_min_fall_distance", 18);
        DWARFISM_SCALE = plugin.getConfig().getDouble("dwarfism.scale", 0.6);
        DWARFISM_ENTITY_RANGE = plugin.getConfig().getDouble("dwarfism.entity_range", 2.25);
        DWARFISM_BLOCK_RANGE = plugin.getConfig().getDouble("dwarfism.block_range", 3.3);
        DWARFISM_MAX_HEALTH = plugin.getConfig().getDouble("dwarfism.max_health", 12);
    }

    /** /dis reload: reads config.yml from disk and uses the new values from now on. */
    public static void reload() {
        plugin.reloadConfig();
        load();
    }
}
