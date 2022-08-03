package com.bgsoftware.wildloaders.nms.v1_16_R3;

import com.bgsoftware.wildloaders.handlers.NPCHandler;
import com.bgsoftware.wildloaders.npc.DummyChannel;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.AxisAlignedBB;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EnumGamemode;
import net.minecraft.server.v1_16_R3.EnumProtocolDirection;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import net.minecraft.server.v1_16_R3.NetworkManager;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_16_R3.PacketPlayInBlockPlace;
import net.minecraft.server.v1_16_R3.PacketPlayInChat;
import net.minecraft.server.v1_16_R3.PacketPlayInFlying;
import net.minecraft.server.v1_16_R3.PacketPlayInHeldItemSlot;
import net.minecraft.server.v1_16_R3.PacketPlayInTransaction;
import net.minecraft.server.v1_16_R3.PacketPlayInUpdateSign;
import net.minecraft.server.v1_16_R3.PacketPlayInWindowClick;
import net.minecraft.server.v1_16_R3.PlayerConnection;
import net.minecraft.server.v1_16_R3.PlayerInteractManager;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.UUID;

public final class ChunkLoaderNPC extends EntityPlayer implements com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC {

    private static final AxisAlignedBB EMPTY_BOUND = new AxisAlignedBB(0D, 0D, 0D, 0D, 0D, 0D);

    private boolean dieCall = false;

    public ChunkLoaderNPC(Location location, UUID uuid) {
        super(((CraftServer) Bukkit.getServer()).getServer(),
                ((CraftWorld) location.getWorld()).getHandle(),
                new GameProfile(uuid, NPCHandler.getName(location.getWorld().getName())),
                new PlayerInteractManager(((CraftWorld) location.getWorld()).getHandle()));

        playerConnection = new DummyPlayerConnection(server, this);

        this.playerInteractManager.setGameMode(EnumGamemode.CREATIVE);
        clientViewDistance = 1;

        fauxSleeping = true;

        spawnIn(world);
        setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        ((WorldServer) world).addPlayerJoin(this);

        super.a(EMPTY_BOUND);
    }

    @Override
    public UUID getUniqueId() {
        return super.getUniqueID();
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return EMPTY_BOUND;
    }

    @Override
    public void die() {
        if (!dieCall) {
            dieCall = true;
            getWorldServer().removePlayer(this);
            dieCall = false;
        } else {
            super.die();
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

        private static Field channelField;
        private static Field socketAddressField;

        static {
            try {
                channelField = NetworkManager.class.getDeclaredField("channel");
                channelField.setAccessible(true);
                socketAddressField = NetworkManager.class.getDeclaredField("socketAddress");
                socketAddressField.setAccessible(true);
            } catch (Exception error) {
                error.printStackTrace();
            }
        }

        DummyNetworkManager() {
            super(EnumProtocolDirection.SERVERBOUND);
            updateFields();
        }

        private void updateFields() {
            try {
                if (channelField != null) {
                    channelField.set(this, new DummyChannel());
                }

                if (socketAddressField != null) {
                    socketAddressField.set(this, null);
                }
            } catch (Exception error) {
                error.printStackTrace();
            }
        }

    }

    public static class DummyPlayerConnection extends PlayerConnection {

        DummyPlayerConnection(MinecraftServer minecraftServer, EntityPlayer entityPlayer) {
            super(minecraftServer, new DummyNetworkManager(), entityPlayer);
        }

        public void a(PacketPlayInWindowClick packetPlayInWindowClick) {

        }

        public void a(PacketPlayInTransaction packetPlayInTransaction) {

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
