package com.bgsoftware.wildloaders.nms.v1_7_R4;

import com.bgsoftware.wildloaders.handlers.NPCHandler;
import com.bgsoftware.wildloaders.npc.DummyChannel;
import net.minecraft.server.v1_7_R4.DamageSource;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.EnumGamemode;
import net.minecraft.server.v1_7_R4.MinecraftServer;
import net.minecraft.server.v1_7_R4.NetworkManager;
import net.minecraft.server.v1_7_R4.Packet;
import net.minecraft.server.v1_7_R4.PacketPlayInBlockDig;
import net.minecraft.server.v1_7_R4.PacketPlayInBlockPlace;
import net.minecraft.server.v1_7_R4.PacketPlayInChat;
import net.minecraft.server.v1_7_R4.PacketPlayInFlying;
import net.minecraft.server.v1_7_R4.PacketPlayInHeldItemSlot;
import net.minecraft.server.v1_7_R4.PacketPlayInTransaction;
import net.minecraft.server.v1_7_R4.PacketPlayInUpdateSign;
import net.minecraft.server.v1_7_R4.PacketPlayInWindowClick;
import net.minecraft.server.v1_7_R4.PlayerConnection;
import net.minecraft.server.v1_7_R4.PlayerInteractManager;
import net.minecraft.server.v1_7_R4.WorldServer;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.UUID;

public final class ChunkLoaderNPC extends EntityPlayer implements com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC {

    public ChunkLoaderNPC(Location location, UUID uuid) {
        super(((CraftServer) Bukkit.getServer()).getServer(),
                ((CraftWorld) location.getWorld()).getHandle(),
                new GameProfile(uuid, NPCHandler.getName(location.getWorld().getName())),
                new PlayerInteractManager(((CraftWorld) location.getWorld()).getHandle()));

        playerConnection = new DummyPlayerConnection(server, this);

        playerInteractManager.setGameMode(EnumGamemode.CREATIVE);
        fallDistance = 0.0F;

        fauxSleeping = true;

        spawnIn(world);
        setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        //noinspection unchecked
        world.players.add(this);
        ((WorldServer) world).getPlayerChunkMap().addPlayer(this);
    }

    @Override
    public UUID getUniqueId() {
        return super.getUniqueID();
    }

    @Override
    public void die() {
        super.die();
        world.players.remove(this);
        ((WorldServer) world).getPlayerChunkMap().removePlayer(this);
    }

    @Override
    public Location getLocation() {
        return getBukkitEntity().getLocation();
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        return false;
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
                channelField = NetworkManager.class.getDeclaredField("m");
                channelField.setAccessible(true);
                socketAddressField = NetworkManager.class.getDeclaredField("n");
                socketAddressField.setAccessible(true);
            } catch (Exception error) {
                error.printStackTrace();
            }
        }

        DummyNetworkManager() {
            super(false);
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
