package de.joja.disabledSMP.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Language of a player = the language of their Minecraft client: German for German clients, English for
 * everyone else. Console and offline players use the default from the config (German).
 */
public enum Lang {

    EN("en", "English"),
    DE("de", "Deutsch");

    public final String code;
    public final String displayName;

    Lang(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    /** The language for the console and offline players: "language" in the config, German if missing or invalid. */
    public static Lang defaultLang() {
        Lang configured = fromCode(DConfig.LANGUAGE);
        return configured != null ? configured : DE;
    }

    public static Lang fromCode(String code) {
        if (code == null)
            return null;
        for (Lang lang : values())
            if (lang.code.equalsIgnoreCase(code) || lang.displayName.equalsIgnoreCase(code))
                return lang;
        if (code.equalsIgnoreCase("english")) return EN;
        if (code.equalsIgnoreCase("german") || code.equalsIgnoreCase("deutsch")) return DE;
        return null;
    }

    // ------------------------------------------------------------ per player (client language)

    /** German clients get German, everyone else (English or any other language) gets English. */
    public static Lang fromLocale(Locale locale) {
        if (locale != null) {
            String language = locale.getLanguage();
            if (language.equals("de")) return DE;
        }
        return EN;
    }

    public static Lang of(Player player) {
        return fromLocale(player.locale());
    }

    /** Offline players, console and command blocks use the configured default language. */
    public static Lang of(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player != null ? of(player) : defaultLang();
    }

    public static Lang of(CommandSender sender) {
        return sender instanceof Player player ? of(player) : defaultLang();
    }

    // ------------------------------------------------------------ picking a translation

    public String pick(String en, String de) {
        return this == DE ? de : en;
    }

    public List<String> pick(List<String> en, List<String> de) {
        return this == DE ? de : en;
    }
}
