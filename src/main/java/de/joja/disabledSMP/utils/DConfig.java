package de.joja.disabledSMP.utils;

import static de.joja.disabledSMP.DisabledSMP.plugin;

public abstract class DConfig {

    public static final String LANGUAGE = plugin.getConfig().getString("language");
    public static final int MAX_KILL_TIME_MS = plugin.getConfig().getInt("max_kill_time_s") * 1000;
}
