package com.bgsoftware.wildloaders.nms.v1_17_R1;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildloaders.handlers.NPCHandler;
import com.bgsoftware.wildloaders.npc.DummyChannel;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPosition;
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
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.phys.AxisAlignedBB;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class ChunkLoaderNPC extends EntityPlayer implements com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC {

    private static final ReflectMethod<Void> SET_GAMEMODE = new ReflectMethod<>(PlayerInteractManager.class,
            1, EnumGamemode.class, EnumGamemode.class);

    private final AxisAlignedBB boundingBox;

    private boolean dieCall = false;

    public ChunkLoaderNPC(MinecraftServer minecraftServer, Location location, UUID uuid) {
        super(minecraftServer, ((CraftWorld) location.getWorld()).getHandle(),
                new GameProfile(uuid, NPCHandler.getName(location.getWorld().getName())));

        this.boundingBox = new AxisAlignedBB(new BlockPosition(location.getX(), location.getY(), location.getZ()));

        this.b = new DummyPlayerConnection(minecraftServer, this);

        SET_GAMEMODE.invoke(this.d, EnumGamemode.b, null);
        clientViewDistance = 1;

        fauxSleeping = true;

        spawnIn(getWorld());
        setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        ((WorldServer) getWorld()).addPlayerJoin(this);

        super.a(this.boundingBox);
    }

    @Override
    public UUID getUniqueId() {
        return super.getUniqueID();
    }

    @Override
    public AxisAlignedBB cs() {
        return this.boundingBox;
    }

    @Override
    public void a(Entity.RemovalReason removalReason) {
        if (!dieCall) {
            dieCall = true;
            removePlayer(getWorldServer(), this);
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

    private static void removePlayer(WorldServer worldServer, EntityPlayer entityPlayer) {
        worldServer.a(entityPlayer, RemovalReason.d);
    }

    public static class DummyNetworkManager extends NetworkManager {

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

        public void a(PacketPlayInWindowClick packetPlayInWindowClick) {

        }

        public void a(PacketPlayInFlying packetPlayInFlying) {

        }

        public void a(PacketPlayInUpdateSign packetPlayInUpdateSign) {

        }

        public void a(PacketPlayInBlockDig packetPlayInBlockDig) {

        }

        public void a(PacketPlayInBlockPlace packetPlayInBlockPlace) {

        }

        public void disconnect(String s) {

        }

        public void a(PacketPlayInHeldItemSlot packetPlayInHeldItemSlot) {

        }

        public void a(PacketPlayInChat packetPlayInChat) {

        }

        public void sendPacket(Packet packet) {

        }

    }

}
