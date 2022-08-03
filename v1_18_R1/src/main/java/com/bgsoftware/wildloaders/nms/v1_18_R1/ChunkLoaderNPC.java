package com.bgsoftware.wildloaders.nms.v1_18_R1;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildloaders.handlers.NPCHandler;
import com.bgsoftware.wildloaders.nms.mapping.Remap;
import com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.net.minecraft.world.entity.Entity;
import com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.net.minecraft.world.level.World;
import com.bgsoftware.wildloaders.npc.DummyChannel;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayInBlockDig;
import net.minecraft.network.protocol.game.PacketPlayInBlockPlace;
import net.minecraft.network.protocol.game.PacketPlayInChat;
import net.minecraft.network.protocol.game.PacketPlayInFlying;
import net.minecraft.network.protocol.game.PacketPlayInHeldItemSlot;
import net.minecraft.network.protocol.game.PacketPlayInUpdateSign;
import net.minecraft.network.protocol.game.PacketPlayInWindowClick;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerInteractManager;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.phys.AxisAlignedBB;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class ChunkLoaderNPC extends EntityPlayer implements com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC {

    private static final ReflectMethod<Void> SET_GAMEMODE = new ReflectMethod<>(PlayerInteractManager.class,
            1, EnumGamemode.class, EnumGamemode.class);

    private static final AxisAlignedBB EMPTY_BOUND = new AxisAlignedBB(0D, 0D, 0D, 0D, 0D, 0D);

    private final World world;

    private boolean dieCall = false;

    @Remap(classPath = "net.minecraft.server.level.ServerPlayer", name = "gameMode", type = Remap.Type.FIELD, remappedName = "d")
    public ChunkLoaderNPC(MinecraftServer minecraftServer, Location location, UUID uuid) {
        super(minecraftServer, ((CraftWorld) location.getWorld()).getHandle(),
                new GameProfile(uuid, NPCHandler.getName(location.getWorld().getName())));

        Entity entity = new Entity(this);
        this.world = entity.getWorld();

        this.b = new DummyPlayerConnection(minecraftServer, this);

        SET_GAMEMODE.invoke(this.d, EnumGamemode.b);
        clientViewDistance = 1;

        fauxSleeping = true;

        spawnIn(this.world.getHandle());
        entity.moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        this.world.addNewPlayer(this);

        super.a(EMPTY_BOUND);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "getUUID",
            type = Remap.Type.METHOD,
            remappedName = "cm")
    @Override
    public UUID getUniqueId() {
        return super.cm();
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "discard",
            type = Remap.Type.METHOD,
            remappedName = "ah")
    @Override
    public void die() {
        ah();
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "getBoundingBoxForCulling",
            type = Remap.Type.METHOD,
            remappedName = "cx")
    @Override
    public AxisAlignedBB cx() {
        return EMPTY_BOUND;
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "remove",
            type = Remap.Type.METHOD,
            remappedName = "a")
    @Override
    public void a(RemovalReason removalReason) {
        if (!dieCall) {
            dieCall = true;
            this.world.removePlayer(this, RemovalReason.d);
            dieCall = false;
        } else {
            super.a(removalReason);
        }
    }

    @Override
    public Location getLocation() {
        return getBukkitEntity().getLocation();
    }

    @Override
    public Player getPlayer() {
        return getBukkitEntity();
    }

    public static class DummyNetworkManager extends NetworkManager {

        @Remap(classPath = "net.minecraft.network.protocol.PacketFlow", name = "SERVERBOUND", type = Remap.Type.FIELD, remappedName = "a")
        @Remap(classPath = "net.minecraft.network.Connection", name = "channel", type = Remap.Type.FIELD, remappedName = "k")
        @Remap(classPath = "net.minecraft.network.Connection", name = "address", type = Remap.Type.FIELD, remappedName = "l")
        DummyNetworkManager() {
            super(EnumProtocolDirection.a);
            this.k = new DummyChannel();
            this.l = null;
        }

    }

    public static class DummyPlayerConnection extends PlayerConnection {

        DummyPlayerConnection(MinecraftServer minecraftServer, EntityPlayer entityPlayer) {
            super(minecraftServer, new DummyNetworkManager(), entityPlayer);
        }

        @Remap(classPath = "net.minecraft.server.network.ServerGamePacketListenerImpl",
                name = "handleContainerClick",
                type = Remap.Type.METHOD,
                remappedName = "a")
        public void a(PacketPlayInWindowClick packetPlayInWindowClick) {

        }

        @Remap(classPath = "net.minecraft.server.network.ServerGamePacketListenerImpl",
                name = "handleMovePlayer",
                type = Remap.Type.METHOD,
                remappedName = "a")
        public void a(PacketPlayInFlying packetPlayInFlying) {

        }

        @Remap(classPath = "net.minecraft.server.network.ServerGamePacketListenerImpl",
                name = "handleSignUpdate",
                type = Remap.Type.METHOD,
                remappedName = "a")
        public void a(PacketPlayInUpdateSign packetPlayInUpdateSign) {

        }

        @Remap(classPath = "net.minecraft.server.network.ServerGamePacketListenerImpl",
                name = "handlePlayerAction",
                type = Remap.Type.METHOD,
                remappedName = "a")
        public void a(PacketPlayInBlockDig packetPlayInBlockDig) {

        }

        @Remap(classPath = "net.minecraft.server.network.ServerGamePacketListenerImpl",
                name = "handleUseItem",
                type = Remap.Type.METHOD,
                remappedName = "a")
        public void a(PacketPlayInBlockPlace packetPlayInBlockPlace) {

        }

        public void disconnect(String s) {

        }

        @Remap(classPath = "net.minecraft.server.network.ServerGamePacketListenerImpl",
                name = "handleSetCarriedItem",
                type = Remap.Type.METHOD,
                remappedName = "a")
        public void a(PacketPlayInHeldItemSlot packetPlayInHeldItemSlot) {

        }

        @Remap(classPath = "net.minecraft.server.network.ServerGamePacketListenerImpl",
                name = "handleChat",
                type = Remap.Type.METHOD,
                remappedName = "a")
        public void a(PacketPlayInChat packetPlayInChat) {

        }

        @Remap(classPath = "net.minecraft.server.network.ServerGamePacketListenerImpl",
                name = "send",
                type = Remap.Type.METHOD,
                remappedName = "a")
        public void a(Packet<?> packet) {

        }

    }

}
