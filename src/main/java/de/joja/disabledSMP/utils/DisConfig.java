package de.joja.disabledSMP.utils;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public abstract class DisConfig {

    public static final String CONFIG_LANGUAGE = plugin.getConfig().getString("language");
    public static final int CONFIG_MAX_KILL_TIME_MS = plugin.getConfig().getInt("max_kill_time_s") * 1000;
}
