package de.joja.disabledSMP.disablities.handlers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import de.joja.disabledSMP.disablities.Disability;
import de.joja.disabledSMP.disablities.handlers.base.DisabilityHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static de.joja.disabledSMP.DisabledSMP.plugin;
import static de.joja.disabledSMP.utils.Utils.random;

public class Schizophrenia extends DisabilityHandler {

    static final EntityType[] POSSIBLE_FAKE_MOBS_OVERWORLD = new EntityType[] {EntityType.CREEPER, EntityType.ZOMBIE, EntityType.SKELETON, EntityType.ENDERMAN};
    static final EntityType[] POSSIBLE_FAKE_MOBS_NETHER = new EntityType[] {EntityType.GHAST, EntityType.HOGLIN, EntityType.SKELETON, EntityType.ENDERMAN, EntityType.PIGLIN_BRUTE};

    private static final AtomicInteger CURRENT_ENTITY_ID = new AtomicInteger(1_000_000);
    private final Map<UUID, Set<Integer>> fakeEntityIDsMap = new HashMap<>();

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

                if (Math.random() < 0.05)
                    spawnFakeMob(player);
                if (Math.random() < 0.005)
                    player.playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1, 1);
            }
        }, 20L, 20L);
    }

    private void spawnFakeMob(Player player) {

        EntityType randomFakeMob;
        if (player.getWorld().getEnvironment().equals(World.Environment.NORMAL))
            randomFakeMob = POSSIBLE_FAKE_MOBS_OVERWORLD[random.nextInt(POSSIBLE_FAKE_MOBS_OVERWORLD.length)];
        else if (player.getWorld().getEnvironment().equals(World.Environment.NETHER))
            randomFakeMob = POSSIBLE_FAKE_MOBS_NETHER[random.nextInt(POSSIBLE_FAKE_MOBS_NETHER.length)];
        else
            return;

        int entityId = CURRENT_ENTITY_ID.getAndIncrement();
        var fakeEntityIDs = fakeEntityIDsMap.get(player.getUniqueId());
        fakeEntityIDs.add(entityId);

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        PacketContainer spawnPacket = manager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);

        spawnPacket.getIntegers().write(0, entityId);
        spawnPacket.getUUIDs().write(0, UUID.randomUUID());
        spawnPacket.getEntityTypeModifier().write(0, randomFakeMob);

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

        spawnPacket.getDoubles()
                .write(0, fakeMobLoc.getX())
                .write(1, fakeMobLoc.getY())
                .write(2, fakeMobLoc.getZ());

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

        PacketContainer lookPacket = manager.createPacket(PacketType.Play.Server.ENTITY_LOOK);

        lookPacket.getIntegers().write(0, entityId);
        lookPacket.getBytes().write(0, yawByte);
        lookPacket.getBytes().write(1, pitchByte);
        lookPacket.getBooleans().write(0, true);

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

    private void createDespawnTimer(Player player, int entityId) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            fakeEntityIDsMap.get(player.getUniqueId()).remove(entityId);
            PacketContainer despawnPacket = manager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            despawnPacket.getIntLists().write(0, List.of(entityId));

            manager.sendServerPacket(player, despawnPacket);
        }, 1000L);
    }



    private Location findSpawnLocation(Player player, int distanceRange) {

        Location checkLoc = null;
        for (int i = 0; i < 8; i++) {
            checkLoc = player.getLocation().add(
                    (Math.random() - 0.5) * distanceRange,
                    0,
                    (Math.random() - 0.5) * distanceRange
            );
            Location foundLoc = tryFindSpawnLocation(checkLoc);
            if (foundLoc != null)
                return foundLoc;
            distanceRange--;
        }

        return checkLoc;
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
            PacketContainer despawnPacket = manager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            despawnPacket.getIntLists().write(0, List.of(id));
            manager.sendServerPacket(player, despawnPacket);
        }
        fakeEntityIDs.clear();
        fakeEntityIDsMap.remove(player.getUniqueId());
    }

}
