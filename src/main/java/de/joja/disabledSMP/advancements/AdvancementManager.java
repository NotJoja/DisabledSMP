package de.joja.disabledSMP.advancements;

import de.joja.disabledSMP.DisabledSMP;
import de.joja.disabledSMP.disablities.Disability;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler.hasDis;

/**
 * Loads the advancement JSONs from resources/advancements and awards them.
 * Every advancement uses the "impossible" trigger and is granted from code via {@link #grant(Player, String)}.
 */
public class AdvancementManager implements Listener {

    // ---- advancement ids (= file names in resources/advancements, without .json) ----
    public static final String ROOT = "root";

    // the three paths of the DisabledSMP tab
    public static final String LIFESTYLE = "lifestyle";
    public static final String CHALLENGES = "challenges";
    public static final String DISABILITIES = "disabilities";

    public static final String GETTING_TO_KNOW_YOURSELF = "getting_to_know_yourself";
    public static final String GOLDEN_DOWNY = "golden_downy";
    public static final String OUT_OF_ORDER = "out_of_order";
    public static final String ACHOO = "achoo";
    public static final String WAS_THAT_REAL = "was_that_real";
    public static final String STILL_STANDING = "still_standing";

    public static final String CANNONBALL = "cannonball";
    public static final String CLUTCH = "clutch";
    public static final String HOMICIDE = "homicide";
    public static final String NAILS_WHEELCHAIR = "nails_wheelchair";

    public static final String YUMMY = "yummy";
    public static final String WHEELCHAIR = "wheelchair";   // not implemented yet -> call grant() when it is
    public static final String ADDICT = "addict";           // not implemented yet -> call grant() when it is

    /** Parents have to be listed before their children. */
    private static final List<String> ALL = List.of(
            ROOT, LIFESTYLE, CHALLENGES, DISABILITIES,
            GETTING_TO_KNOW_YOURSELF, GOLDEN_DOWNY, OUT_OF_ORDER, ACHOO, WAS_THAT_REAL, STILL_STANDING,
            CANNONBALL, CLUTCH, HOMICIDE, NAILS_WHEELCHAIR,
            YUMMY, WHEELCHAIR, ADDICT
    );

    private static final String CRITERION = "done";

    // Clutch: a Balance Disorder push only counts for a limited time, and the MLG has to start from this fall distance
    private static final long PUSH_VALID_MS = 30_000;
    private static final float CLUTCH_MIN_FALL_DISTANCE = 15f;

    private final JavaPlugin plugin;
    private final Map<UUID, Long> pushedAt = new HashMap<>();

    /** Set while the Overweight explosion is being created, so deaths caused by it can be credited. */
    private Player overweightExploder = null;

    public AdvancementManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // ------------------------------------------------------------------ registering

    public void registerAdvancements() {

        boolean needsReload = false;

        for (String id : ALL) {
            NamespacedKey key = new NamespacedKey(plugin, id);

            try (InputStream stream = plugin.getResource("advancements/" + id + ".json")) {
                if (stream == null) {
                    plugin.getLogger().severe("Could not find advancements/" + id + ".json!");
                    continue;
                }
                String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

                // Paper saves loaded advancements into the world's datapacks/bukkit folder and loads them from there
                // on the next start, so an advancement from an older version of the plugin (old icon, ...) is already
                // registered. Overwrite the saved copy and reload the data once so the new one is used.
                if (Bukkit.getAdvancement(key) != null) {
                    if (refreshSavedAdvancement(key, json))
                        needsReload = true;
                    continue;
                }

                Bukkit.getUnsafe().loadAdvancement(key, json);
            } catch (IOException | IllegalArgumentException e) {
                plugin.getLogger().severe("Failed to register advancement '" + id + "'!");
                e.printStackTrace();
            }
        }

        if (needsReload) {
            plugin.getLogger().info("Updated outdated advancements, reloading data...");
            Bukkit.getScheduler().runTask(plugin, Bukkit::reloadData);
        }

        plugin.getLogger().info("Registered DisabledSMP advancements!");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Advancements that depend on the player's state (not on a single event)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers())
                checkDisabilityAdvancements(player);
        }, 40L, 40L);
    }

    /** Overwrites the copy Paper saved in world/datapacks/bukkit if it differs. Returns true if it was changed. */
    private boolean refreshSavedAdvancement(NamespacedKey key, String json) {
        if (Bukkit.getWorlds().isEmpty())
            return false;

        java.nio.file.Path file = Bukkit.getWorlds().get(0).getWorldFolder().toPath()
                .resolve("datapacks").resolve("bukkit").resolve("data").resolve(key.getNamespace())
                .resolve("advancements").resolve(key.getKey() + ".json");

        try {
            if (java.nio.file.Files.exists(file) && java.nio.file.Files.readString(file, StandardCharsets.UTF_8).equals(json))
                return false;

            java.nio.file.Files.createDirectories(file.getParent());
            java.nio.file.Files.writeString(file, json, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning("Could not update the saved advancement '" + key.getKey() + "': " + e);
            return false;
        }
    }

    // ------------------------------------------------------------------ granting

    public void grant(Player player, String id) {
        Advancement advancement = Bukkit.getAdvancement(new NamespacedKey(plugin, id));
        if (advancement == null)
            return;

        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (!progress.isDone())
            progress.awardCriteria(CRITERION);
    }

    private boolean has(Player player, String id) {
        Advancement advancement = Bukkit.getAdvancement(new NamespacedKey(plugin, id));
        return advancement != null && player.getAdvancementProgress(advancement).isDone();
    }

    private void checkDisabilityAdvancements(Player player) {
        List<Disability> disabilities = DisabledSMP.plugin.disabilityMap.get(player.getUniqueId());
        if (disabilities == null)
            return;

        if (!has(player, GOLDEN_DOWNY) && !Disability.enabled().isEmpty() && disabilities.containsAll(Disability.enabled()))
            grant(player, GOLDEN_DOWNY);

        if (!has(player, OUT_OF_ORDER)
                && disabilities.contains(Disability.PARALYZED_LEG)
                && disabilities.contains(Disability.PARALYZED_ARM))
            grant(player, OUT_OF_ORDER);
    }

    // ------------------------------------------------------------------ hooks called from other classes

    /** Called by BalanceDisorder whenever it pushes a player. */
    public void markPushed(Player player) {
        pushedAt.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /** Called by Overweight around world.createExplosion(...). Pass null afterwards. */
    public void setOverweightExploder(Player exploder) {
        this.overweightExploder = exploder;
    }

    // ------------------------------------------------------------------ listeners

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // The root has to be granted for the tab to show up, the paths for their branches to show
        for (String id : List.of(ROOT, LIFESTYLE, CHALLENGES, DISABILITIES))
            grant(event.getPlayer(), id);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        pushedAt.remove(event.getPlayer().getUniqueId());
    }

    /** Was that real? -> die to a creeper while having schizophrenia. Cannonball! -> kill a player with the overweight explosion. */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player died = event.getEntity();

        // Cannonball!
        if (overweightExploder != null && overweightExploder != died
                && hasDis(overweightExploder, Disability.OVERWEIGHT)) {
            EntityDamageEvent last = died.getLastDamageCause();
            if (last != null && (last.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION
                    || last.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION))
                grant(overweightExploder, CANNONBALL);
        }

        // Was that real?
        if (hasDis(died, Disability.SCHIZOPHRENIA)
                && died.getLastDamageCause() instanceof EntityDamageByEntityEvent byEntity
                && byEntity.getDamager() instanceof Creeper)
            grant(died, WAS_THAT_REAL);
    }

    /** Genocide! -> kill a baby zombie while having dwarfism. */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Zombie zombie) || !zombie.isBaby())
            return;

        Player killer = zombie.getKiller();
        if (killer != null && hasDis(killer, Disability.DWARFISM))
            grant(killer, HOMICIDE);
    }

    /** Clutch! -> place water from high enough up and actually land in it, after a Balance Disorder push. */
    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();

        if (event.getBucket() != Material.WATER_BUCKET)
            return;

        Long pushed = pushedAt.get(player.getUniqueId());
        if (pushed == null || System.currentTimeMillis() - pushed > PUSH_VALID_MS)
            return;

        if (player.getFallDistance() < CLUTCH_MIN_FALL_DISTANCE)
            return;

        // Wait until the player is actually standing in the water (max 2 seconds)
        int[] ticks = {0};
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!player.isOnline() || player.isDead()) {
                task.cancel();
                return;
            }

            if (player.isInWater()) {
                grant(player, CLUTCH);
                pushedAt.remove(player.getUniqueId());
                task.cancel();
                return;
            }

            if (player.isOnGround() || ++ticks[0] > 40)
                task.cancel();
        }, 1L, 1L);
    }
}
