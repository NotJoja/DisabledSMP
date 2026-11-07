package de.joja.disabledSMP.disablities.handlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import de.joja.disabledSMP.utils.DConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.utils.Utils.random;

public class Schizophrenia extends DisabilityHandler {

    static final EntityType[] POSSIBLE_FAKE_MOBS_OVERWORLD = new EntityType[] {EntityType.CREEPER, EntityType.ZOMBIE, EntityType.SKELETON, EntityType.ENDERMAN};
    static final EntityType[] POSSIBLE_FAKE_MOBS_NETHER = new EntityType[] {EntityType.GHAST, EntityType.HOGLIN, EntityType.ZOMBIFIED_PIGLIN, EntityType.SKELETON, EntityType.ENDERMAN, EntityType.PIGLIN_BRUTE};

    /** Hungry players (this food level or less) mostly see food: cows, or hoglins in the nether. */
    private static final int HUNGRY_FOOD_LEVEL = DConfig.SCHIZOPHRENIA_HUNGRY_FOOD_LEVEL;
    private static final double HUNGRY_FOOD_MOB_CHANCE = DConfig.SCHIZOPHRENIA_HUNGRY_MOB_CHANCE;

    /** Ores that can look like diamond ore (only on the client of the player). */
    private static final Set<Material> FAKEABLE_ORES = Set.of(
            Material.COAL_ORE, Material.IRON_ORE, Material.COPPER_ORE, Material.GOLD_ORE,
            Material.REDSTONE_ORE, Material.LAPIS_ORE,
            Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.DEEPSLATE_GOLD_ORE, Material.DEEPSLATE_REDSTONE_ORE, Material.DEEPSLATE_LAPIS_ORE);

    /** Chance per second that a fake mob appears. The nether has fewer mobs to choose from and less room. */
    private static final double FAKE_MOB_CHANCE = DConfig.SCHIZOPHRENIA_FAKE_MOB_CHANCE;
    private static final double FAKE_MOB_CHANCE_NETHER = DConfig.SCHIZOPHRENIA_FAKE_MOB_CHANCE_NETHER;

    /** Chance per second that a hallucination of fake diamond ores starts (about once every 1-2 minutes). */
    private static final double FAKE_ORE_CHANCE = DConfig.SCHIZOPHRENIA_FAKE_ORE_CHANCE;
    private static final int FAKE_ORE_RADIUS = DConfig.SCHIZOPHRENIA_FAKE_ORE_RADIUS;
    private static final int FAKE_ORE_MAX_AMOUNT = Math.max(1, DConfig.SCHIZOPHRENIA_FAKE_ORE_MAX_AMOUNT);
    private static final long FAKE_ORE_TICKS = 20L * DConfig.SCHIZOPHRENIA_FAKE_ORE_DURATION_S;

    /** The blocks that currently look like diamond ore for a player (so they can be reset). */
    private final Map<UUID, Set<Location>> fakeOresMap = new HashMap<>();

    private static final AtomicInteger CURRENT_ENTITY_ID = new AtomicInteger(1_000_000);
    private final Map<UUID, Set<Integer>> fakeEntityIDsMap = new HashMap<>();

    /** Team with collisions turned off: the fake mobs are in it (by their uuid), so they can't push the player. */
    private static final String NO_COLLISION_TEAM = "dismp_fakemobs";
    private final Map<Integer, String> fakeEntryByEntityId = new HashMap<>();

    public Schizophrenia() {
        this.enName = "Schizophrenia";
        this.enDataName = toDataName(enName);
        this.enDescription = List.of(
                "You experience hallucinations",
                "like fake mobs or sounds"
        );

        this.deName = "Schizophrenie";
        this.deDataName = toDataName(deName);
        this.deDescription = List.of(
                "Du erlebst Halluzinationen",
                "wie fake Mobs oder Sounds"
        );

        initIcons();
        
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!hasDis(player, Disability.SCHIZOPHRENIA))
                    continue;

                boolean nether = player.getWorld().getEnvironment().equals(World.Environment.NETHER);
                if (Math.random() < (nether ? FAKE_MOB_CHANCE_NETHER : FAKE_MOB_CHANCE))
                    spawnFakeMob(player);

                if (Math.random() < FAKE_ORE_CHANCE)
                    showFakeDiamondOres(player);

                if (player.getWorld().getEnvironment().equals(World.Environment.NETHER)) {
                    if (Math.random() < 0.008) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT, 1, 1);
                    }
                    if (Math.random() < 0.008) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_WARN, 1, 1);
                    }
                } else {
                    if (Math.random() < 0.01)
                        player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1, 1);
                }
            }
        }, 20L, 20L);
    }

    private void spawnFakeMob(Player player) {

        boolean hungry = player.getFoodLevel() <= HUNGRY_FOOD_LEVEL;
        boolean seesFood = hungry && random.nextDouble() < HUNGRY_FOOD_MOB_CHANCE;

        EntityType randomFakeMob;
        if (player.getWorld().getEnvironment().equals(World.Environment.NORMAL))
            randomFakeMob = seesFood ? EntityType.COW
                    : POSSIBLE_FAKE_MOBS_OVERWORLD[random.nextInt(POSSIBLE_FAKE_MOBS_OVERWORLD.length)];
        else if (player.getWorld().getEnvironment().equals(World.Environment.NETHER))
            randomFakeMob = seesFood ? EntityType.HOGLIN
                    : POSSIBLE_FAKE_MOBS_NETHER[random.nextInt(POSSIBLE_FAKE_MOBS_NETHER.length)];
        else
            randomFakeMob = EntityType.ENDERMAN;

        int distanceRange = 16;
        if (randomFakeMob == EntityType.GHAST)
            distanceRange = 48;

        Location fakeMobLoc;
        if (randomFakeMob == EntityType.GHAST)
            fakeMobLoc = player.getLocation().add(
                    (Math.random() - 0.5) * distanceRange,
                    (Math.random() - 0.5) * distanceRange,
                    (Math.random() - 0.5) * distanceRange
            );
        else
            fakeMobLoc = findSpawnLocation(player, distanceRange);

        // ground mobs only spawn on a solid block, no place found -> no mob this time
        if (fakeMobLoc == null) {
            DConfig.debug("No floor found for a fake " + randomFakeMob + " near " + player.getName());
            return;
        }


        int entityId = CURRENT_ENTITY_ID.getAndIncrement();
        UUID fakeUuid = UUID.randomUUID();
        DConfig.debug("Fake " + randomFakeMob + " for " + player.getName() + " at " + fakeMobLoc.getBlockX() + " " + fakeMobLoc.getBlockY() + " " + fakeMobLoc.getBlockZ());
        noCollisionTeam().addEntry(fakeUuid.toString());
        fakeEntryByEntityId.put(entityId, fakeUuid.toString());
        var fakeEntityIDs = fakeEntityIDsMap.get(player.getUniqueId());
        fakeEntityIDs.add(entityId);

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        PacketContainer spawnPacket = NmsPackets.spawnEntity(
                entityId, fakeUuid, randomFakeMob,
                fakeMobLoc.getX(), fakeMobLoc.getY(), fakeMobLoc.getZ()
        );

        manager.sendServerPacket(player, spawnPacket);

        rotateFakeMobToPlayer(player, fakeMobLoc, entityId);
        createLookTimers(player, fakeMobLoc, entityId);
        createDespawnTimer(player, entityId);
    }

    private void rotateFakeMobToPlayer(Player player, Location fakeMobLoc, int entityId) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        Location playerLoc = player.getLocation();
        Vector dir = playerLoc.toVector().subtract(fakeMobLoc.toVector()).normalize();

        float yaw = (float) Math.toDegrees(Math.atan2(-dir.getX(), dir.getZ()) / 360);
        float pitch = (float) Math.toDegrees(-Math.atan2(dir.getY(), dir.length())) / 360;
        byte yawByte = (byte) (yaw * 256);
        byte pitchByte = (byte) (pitch * 256);

        PacketContainer lookPacket = NmsPackets.entityLook(entityId, yawByte, pitchByte);

        manager.sendServerPacket(player, lookPacket);

        PacketContainer headRotatePacket = manager.createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);

        headRotatePacket.getIntegers().write(0, entityId);
        headRotatePacket.getBytes().write(0, yawByte);

        manager.sendServerPacket(player, headRotatePacket);
    }

    private void createLookTimers(Player player, Location fakeMobLocation, int entityID) {
        int lookInterval = 50;
        for (int i = 1; i < 1000/lookInterval; i++) {
            int delay = i * lookInterval;
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    rotateFakeMobToPlayer(player, fakeMobLocation, entityID), delay);
        }
    }

    private static Team noCollisionTeam() {
        var scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(NO_COLLISION_TEAM);
        if (team == null) {
            team = scoreboard.registerNewTeam(NO_COLLISION_TEAM);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        }
        return team;
    }

    private void forgetFakeEntry(int entityId) {
        String entry = fakeEntryByEntityId.remove(entityId);
        if (entry != null)
            noCollisionTeam().removeEntry(entry);
    }

    private void createDespawnTimer(Player player, int entityId) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            var ids = fakeEntityIDsMap.get(player.getUniqueId());
            if (ids != null)
                ids.remove(entityId);
            forgetFakeEntry(entityId);
            manager.sendServerPacket(player, NmsPackets.removeEntity(entityId));
        }, 1000L);
    }



    /** Some ores nearby look like diamond ore for a while, they aren't. */
    private void showFakeDiamondOres(Player player) {
        if (!player.getWorld().getEnvironment().equals(World.Environment.NORMAL))
            return;

        Location center = player.getLocation();
        World world = center.getWorld();

        List<Block> candidates = new ArrayList<>();
        for (int x = -FAKE_ORE_RADIUS; x <= FAKE_ORE_RADIUS; x++)
            for (int y = -FAKE_ORE_RADIUS; y <= FAKE_ORE_RADIUS; y++)
                for (int z = -FAKE_ORE_RADIUS; z <= FAKE_ORE_RADIUS; z++) {
                    int bx = center.getBlockX() + x;
                    int by = center.getBlockY() + y;
                    int bz = center.getBlockZ() + z;
                    if (by < world.getMinHeight() || by >= world.getMaxHeight() || !world.isChunkLoaded(bx >> 4, bz >> 4))
                        continue;

                    Block block = world.getBlockAt(bx, by, bz);
                    if (FAKEABLE_ORES.contains(block.getType()) && isExposed(block))
                        candidates.add(block);
                }

        if (candidates.isEmpty()) {
            DConfig.debug("No ore found for fake diamond ores near " + player.getName());
            return;
        }

        Collections.shuffle(candidates, random);
        int amount = 1 + random.nextInt(FAKE_ORE_MAX_AMOUNT);
        var fakeOres = fakeOresMap.computeIfAbsent(player.getUniqueId(), id -> new HashSet<>());

        for (Block block : candidates.subList(0, Math.min(amount, candidates.size()))) {
            Location loc = block.getLocation();
            Material fake = block.getType().name().startsWith("DEEPSLATE_")
                    ? Material.DEEPSLATE_DIAMOND_ORE : Material.DIAMOND_ORE;

            fakeOres.add(loc);
            player.sendBlockChange(loc, fake.createBlockData());

            Bukkit.getScheduler().runTaskLater(plugin, () -> resetFakeOre(player, loc), FAKE_ORE_TICKS);
        }
    }

    /** Only blocks you can actually see (a side that touches something see-through). */
    private static boolean isExposed(Block block) {
        for (BlockFace face : new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH,
                BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST})
            if (!block.getRelative(face).getType().isOccluding())
                return true;
        return false;
    }

    private void resetFakeOre(Player player, Location loc) {
        var fakeOres = fakeOresMap.get(player.getUniqueId());
        if (fakeOres == null || !fakeOres.remove(loc))
            return;     // already reset

        if (player.isOnline())
            player.sendBlockChange(loc, loc.getBlock().getBlockData());
    }

    private Location findSpawnLocation(Player player, int distanceRange) {

        // plenty of tries: in the nether (tunnels, lava) a lot of random spots have no floor
        for (int i = 0; i < 24; i++) {
            Location checkLoc = player.getLocation().add(
                    (Math.random() - 0.5) * distanceRange,
                    0,
                    (Math.random() - 0.5) * distanceRange
            );
            Location foundLoc = tryFindSpawnLocation(checkLoc);
            if (foundLoc != null)
                return foundLoc;
            distanceRange = Math.max(6, distanceRange - 1);
        }

        return null;
    }

    private Location tryFindSpawnLocation(Location base) {

        World world = base.getWorld();
        int x = base.getBlockX();
        int z = base.getBlockZ();

        for (int y = base.getBlockY()+6; y > base.getBlockY()-6; y--) {

            Block ground = world.getBlockAt(x, y, z);
            Block above1 = world.getBlockAt(x, y + 1, z);
            Block above2 = world.getBlockAt(x, y + 2, z);

            if (ground.getType().isSolid()
                    && !above1.getType().isSolid()
                    && !above2.getType().isSolid()) {

                return new Location(world, x + 0.5, y + 1, z + 0.5);
            }
        }

        return null;
    }



    @Override
    public void addToPlayer(Player player) {
        fakeEntityIDsMap.put(player.getUniqueId(), new HashSet<>());
    }

    @Override
    public void removeFromPlayer(Player player) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        var fakeEntityIDs = fakeEntityIDsMap.get(player.getUniqueId());
        for (int id : fakeEntityIDs) {
            forgetFakeEntry(id);
            manager.sendServerPacket(player, NmsPackets.removeEntity(id));
        }
        fakeEntityIDs.clear();
        fakeEntityIDsMap.remove(player.getUniqueId());

        var fakeOres = fakeOresMap.remove(player.getUniqueId());
        if (fakeOres != null)
            for (Location loc : fakeOres)
                player.sendBlockChange(loc, loc.getBlock().getBlockData());
    }

}
