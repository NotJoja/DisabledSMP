package de.joja.disabledSMP.utils;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public abstract class DConfig {

    public static final String LANGUAGE = plugin.getConfig().getString("language");
    public static final String RESOURCE_PACK_URL = plugin.getConfig().getString("resource_pack_url",
            "https://raw.githubusercontent.com/NotJoja/DismpTP/master/DisabledSMP/dismp.zip");
    public static final String RESOURCE_PACK_SHA1 = plugin.getConfig().getString("resource_pack_sha1",
            "auto");
    public static final String COLORBLIND_PACK_URL = plugin.getConfig().getString("colorblind_pack_url",
            "https://raw.githubusercontent.com/NotJoja/DismpTP/master/Colorblind/Colorblind.zip");
    public static final String COLORBLIND_PACK_SHA1 = plugin.getConfig().getString("colorblind_pack_sha1", "auto");
    public static final int MAX_KILL_TIME_MS = plugin.getConfig().getInt("max_kill_time_s") * 1000;

    // ---- housekeeping
    public static final boolean DEBUG = plugin.getConfig().getBoolean("debug", false);

    /** Prints to the console if "debug" is on in the config. */
    public static void debug(String message) {
        if (DEBUG)
            plugin.getLogger().info("[debug] " + message);
    }

    // ---- how many disabilities a player has (-1 = off / default)
    public static final int FIRST_JOIN_DISABILITIES = plugin.getConfig().getInt("first_join_disabilities", -1);
    public static final int MIN_DISABILITIES = plugin.getConfig().getInt("min_disabilities", -1);
    public static final int MAX_DISABILITIES = plugin.getConfig().getInt("max_disabilities", -1);

    // ---- Asthma
    public static final double ASTHMA_MIN_LIMIT_S = plugin.getConfig().getDouble("asthma.min_limit_s", 20);
    public static final double ASTHMA_MAX_LIMIT_S = plugin.getConfig().getDouble("asthma.max_limit_s", 30);
    public static final double ASTHMA_SPRINT_GAIN = plugin.getConfig().getDouble("asthma.sprint_gain", 1.0);
    public static final double ASTHMA_REST_RECOVERY = plugin.getConfig().getDouble("asthma.rest_recovery", 0.5);
    public static final double ASTHMA_WINDED_RECOVERY = plugin.getConfig().getDouble("asthma.winded_recovery", 0.75);
    public static final boolean ASTHMA_BLOCK_SPRINT = plugin.getConfig().getBoolean("asthma.block_sprint", true);

    // ---- Mentally Disabled
    public static final double MENTALLY_DISABLED_MISCRAFT_CHANCE = plugin.getConfig().getDouble("mentally_disabled.miscraft_chance", 0.5);
    public static final boolean MENTALLY_DISABLED_CHAT_SHUFFLE = plugin.getConfig().getBoolean("mentally_disabled.chat_shuffle", true);

    // ---- Schizophrenia
    public static final double SCHIZOPHRENIA_FAKE_MOB_CHANCE = plugin.getConfig().getDouble("schizophrenia.fake_mob_chance", 0.05);
    public static final double SCHIZOPHRENIA_FAKE_MOB_CHANCE_NETHER = plugin.getConfig().getDouble("schizophrenia.fake_mob_chance_nether", 0.10);
    public static final int SCHIZOPHRENIA_HUNGRY_FOOD_LEVEL = plugin.getConfig().getInt("schizophrenia.hungry_food_level", 6);
    public static final double SCHIZOPHRENIA_HUNGRY_MOB_CHANCE = plugin.getConfig().getDouble("schizophrenia.hungry_mob_chance", 0.7);
    public static final double SCHIZOPHRENIA_FAKE_ORE_CHANCE = plugin.getConfig().getDouble("schizophrenia.fake_ore_chance", 0.012);
    public static final int SCHIZOPHRENIA_FAKE_ORE_RADIUS = plugin.getConfig().getInt("schizophrenia.fake_ore_radius", 10);
    public static final int SCHIZOPHRENIA_FAKE_ORE_MAX_AMOUNT = plugin.getConfig().getInt("schizophrenia.fake_ore_max_amount", 3);
    public static final int SCHIZOPHRENIA_FAKE_ORE_DURATION_S = plugin.getConfig().getInt("schizophrenia.fake_ore_duration_s", 45);

    // ---- Overweight
    public static final double OVERWEIGHT_HUNGER_DRAIN = plugin.getConfig().getDouble("overweight.hunger_drain", 0.03);
    public static final double OVERWEIGHT_MOVEMENT_SPEED = plugin.getConfig().getDouble("overweight.movement_speed", 0.08);
    public static final double OVERWEIGHT_JUMP_STRENGTH = plugin.getConfig().getDouble("overweight.jump_strength", 0.36);
    public static final double OVERWEIGHT_EXPLOSION_MIN_FALL = plugin.getConfig().getDouble("overweight.explosion_min_fall_distance", 18);

    // ---- Dwarfism
    public static final double DWARFISM_SCALE = plugin.getConfig().getDouble("dwarfism.scale", 0.6);
    public static final double DWARFISM_ENTITY_RANGE = plugin.getConfig().getDouble("dwarfism.entity_range", 2.25);
    public static final double DWARFISM_BLOCK_RANGE = plugin.getConfig().getDouble("dwarfism.block_range", 3.3);
    public static final double DWARFISM_MAX_HEALTH = plugin.getConfig().getDouble("dwarfism.max_health", 12);
}
