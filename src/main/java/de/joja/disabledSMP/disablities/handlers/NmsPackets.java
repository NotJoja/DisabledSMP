package de.joja.disabledSMP.disablities.handlers;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Constructor;
import java.util.UUID;

/**
 * Builds the packets used for fake entities through the vanilla public constructors.
 * ProtocolLib's structure modifiers (e.g. getEntityTypeModifier) can't find the entity type
 * field of the spawn packet on 26.3, so we don't depend on them for these packets.
 */
final class NmsPackets {

    private NmsPackets() {}

    private static final String PACKET_PKG = "net.minecraft.network.protocol.game.";

    static PacketContainer spawnEntity(int entityId, UUID uuid, EntityType type, double x, double y, double z) {
        try {
            Class<?> craftEntityType = Class.forName("org.bukkit.craftbukkit.entity.CraftEntityType");
            Object nmsType = craftEntityType.getMethod("bukkitToMinecraft", EntityType.class).invoke(null, type);

            Class<?> nmsTypeClass = Class.forName("net.minecraft.world.entity.EntityType");
            Class<?> vec3Class = Class.forName("net.minecraft.world.phys.Vec3");
            Object zeroVec = vec3Class.getField("ZERO").get(null);

            // (id, uuid, x, y, z, xRot, yRot, type, data, movement, yHeadRot)
            Constructor<?> constructor = Class.forName(PACKET_PKG + "ClientboundAddEntityPacket").getConstructor(
                    int.class, UUID.class, double.class, double.class, double.class,
                    float.class, float.class, nmsTypeClass, int.class, vec3Class, double.class
            );

            return PacketContainer.fromPacket(
                    constructor.newInstance(entityId, uuid, x, y, z, 0f, 0f, nmsType, 0, zeroVec, 0.0)
            );
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not create spawn packet", e);
        }
    }

    static PacketContainer entityLook(int entityId, byte yawByte, byte pitchByte) {
        try {
            // Rot(id, yRot, xRot, onGround)
            Constructor<?> constructor = Class.forName(PACKET_PKG + "ClientboundMoveEntityPacket$Rot")
                    .getConstructor(int.class, byte.class, byte.class, boolean.class);
            return PacketContainer.fromPacket(constructor.newInstance(entityId, yawByte, pitchByte, true));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not create look packet", e);
        }
    }

    static PacketContainer removeEntity(int entityId) {
        try {
            Constructor<?> constructor = Class.forName(PACKET_PKG + "ClientboundRemoveEntitiesPacket")
                    .getConstructor(int[].class);
            return PacketContainer.fromPacket(constructor.newInstance((Object) new int[] {entityId}));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not create remove packet", e);
        }
    }
}
