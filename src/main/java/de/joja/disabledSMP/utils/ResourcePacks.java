package de.joja.disabledSMP.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.UUID;
import java.util.function.Supplier;

import static de.joja.disabledSMP.DisabledSMP.plugin;

/**
 * The resource packs of the plugin. Both are active at the same time (packs stack): everyone gets the main pack
 * (models, icons, lang files), players with Color Blindness additionally get the colorblind pack on top.
 */
public abstract class ResourcePacks {

    public static final ResourcePack MAIN = new ResourcePack(
            "main", () -> DConfig.RESOURCE_PACK_URL, () -> DConfig.RESOURCE_PACK_SHA1,
            "This server requires the DisabledSMP resource pack!",
            "Dieser Server benötigt das DisabledSMP Resource Pack!");

    public static final ResourcePack COLORBLIND = new ResourcePack(
            "colorblind", () -> DConfig.COLORBLIND_PACK_URL, () -> DConfig.COLORBLIND_PACK_SHA1,
            "Every colorblind player must use this resource pack!",
            "Jeder farbenblinde Spieler muss dieses Resource Pack benutzen!");

    /** Starts calculating the hashes of packs with resource_pack_sha1 / colorblind_pack_sha1 set to "auto". */
    public static void startAutoHash() {
        MAIN.startAutoHash();
        COLORBLIND.startAutoHash();
    }

    public static final class ResourcePack {

        private static final String AUTO = "auto";

        private final String name;
        private final UUID id;
        private final Supplier<String> url;
        private final Supplier<String> sha1Config;
        private final String enPrompt;
        private final String dePrompt;

        /** SHA-1 of the pack, calculated by downloading it (only if the config says "auto"). */
        private volatile byte[] autoHash = null;

        /** The last download attempt failed: the pack isn't reachable, so don't force it on players. */
        private volatile boolean unreachable = false;

        private ResourcePack(String name, Supplier<String> url, Supplier<String> sha1Config, String enPrompt, String dePrompt) {
            this.name = name;
            // a fixed id per pack, so sending it again replaces the old copy and it can be removed again
            this.id = UUID.nameUUIDFromBytes(("disabledsmp:" + name).getBytes(StandardCharsets.UTF_8));
            this.url = url;
            this.sha1Config = sha1Config;
            this.enPrompt = enPrompt;
            this.dePrompt = dePrompt;
        }

        /** Adds the pack on top of the packs the player already has. */
        public void send(Player player) {
            // the pack is sent as required, a pack that can't be downloaded would only get players stuck or kicked
            if (unreachable) {
                plugin.getLogger().warning("Not sending the " + name + " resource pack to " + player.getName()
                        + ", it could not be downloaded from " + url.get());
                return;
            }

            player.addResourcePack(id, url.get(), currentHash(), Lang.of(player).pick(enPrompt, dePrompt), true);
        }

        public void remove(Player player) {
            player.removeResourcePack(id);
        }

        /**
         * With "auto" the plugin downloads the pack itself and works out the hash, now and every few minutes, so a new
         * pack (and its changed hash) is picked up without touching the config.
         */
        private void startAutoHash() {
            if (!AUTO.equalsIgnoreCase(configuredHash()))
                return;

            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::refreshAutoHash, 0L, 20L * 60 * 5);
        }

        private void refreshAutoHash() {
            try {
                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

                HttpResponse<InputStream> response = client.send(
                        HttpRequest.newBuilder(URI.create(url.get())).timeout(Duration.ofSeconds(60)).GET().build(),
                        HttpResponse.BodyHandlers.ofInputStream()
                );

                if (response.statusCode() != 200)
                    throw new IllegalStateException("HTTP " + response.statusCode());

                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                try (InputStream in = response.body()) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1)
                        digest.update(buffer, 0, read);
                }

                byte[] hash = digest.digest();
                if (!Arrays.equals(hash, autoHash))
                    plugin.getLogger().info("Resource pack '" + name + "' SHA-1: " + HexFormat.of().formatHex(hash));
                autoHash = hash;
                unreachable = false;
            } catch (Exception e) {
                unreachable = true;
                plugin.getLogger().warning("Could not calculate the hash of the " + name + " resource pack from " + url.get() + ": " + e);
            }
        }

        private String configuredHash() {
            String configured = sha1Config.get();
            return configured == null ? "" : configured.trim();
        }

        private byte[] currentHash() {
            String configured = configuredHash();

            if (configured.equalsIgnoreCase(AUTO))
                return autoHash;       // null until the first download is done -> sent without a hash

            return parseHash(configured);
        }

        /** A hash written into the config. Empty or invalid -> no hash. */
        private byte[] parseHash(String hex) {
            if (hex.isEmpty())
                return null;

            try {
                byte[] hash = HexFormat.of().parseHex(hex);
                if (hash.length == 20)
                    return hash;
            } catch (IllegalArgumentException ignored) {
            }

            plugin.getLogger().warning("The sha1 of the " + name + " resource pack in config.yml is not valid (use \"auto\" or a 40 character SHA-1), ignoring it");
            return null;
        }
    }
}
