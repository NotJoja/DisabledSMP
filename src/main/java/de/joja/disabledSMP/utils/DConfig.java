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
}
