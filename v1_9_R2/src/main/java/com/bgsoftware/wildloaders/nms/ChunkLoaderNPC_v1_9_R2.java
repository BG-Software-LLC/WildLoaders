package com.bgsoftware.wildloaders.nms;

import com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC;
import com.bgsoftware.wildloaders.npc.DummyChannel;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_9_R2.DamageSource;
import net.minecraft.server.v1_9_R2.EntityPlayer;
import net.minecraft.server.v1_9_R2.EnumProtocolDirection;
import net.minecraft.server.v1_9_R2.MinecraftServer;
import net.minecraft.server.v1_9_R2.NetworkManager;
import net.minecraft.server.v1_9_R2.Packet;
import net.minecraft.server.v1_9_R2.PacketPlayInBlockDig;
import net.minecraft.server.v1_9_R2.PacketPlayInBlockPlace;
import net.minecraft.server.v1_9_R2.PacketPlayInChat;
import net.minecraft.server.v1_9_R2.PacketPlayInFlying;
import net.minecraft.server.v1_9_R2.PacketPlayInHeldItemSlot;
import net.minecraft.server.v1_9_R2.PacketPlayInTransaction;
import net.minecraft.server.v1_9_R2.PacketPlayInUpdateSign;
import net.minecraft.server.v1_9_R2.PacketPlayInWindowClick;
import net.minecraft.server.v1_9_R2.PlayerConnection;
import net.minecraft.server.v1_9_R2.PlayerInteractManager;
import net.minecraft.server.v1_9_R2.WorldServer;
import net.minecraft.server.v1_9_R2.WorldSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;

import java.lang.reflect.Field;
import java.util.UUID;

public final class ChunkLoaderNPC_v1_9_R2 extends EntityPlayer implements ChunkLoaderNPC {

    public ChunkLoaderNPC_v1_9_R2(Location location, UUID uuid){
        super(((CraftServer) Bukkit.getServer()).getServer(), ((CraftWorld) location.getWorld()).getHandle(),
                new GameProfile(uuid, "Loader-" + location.getWorld().getName()), new PlayerInteractManager(((CraftWorld) location.getWorld()).getHandle()));

        playerConnection = new DummyPlayerConnection(server, this);

        playerInteractManager.setGameMode(WorldSettings.EnumGamemode.CREATIVE);
        fallDistance = 0.0F;

        fauxSleeping = true;

        spawnIn(world);
        setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

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
    protected boolean damageEntity0(DamageSource damagesource, float f) {
        return false;
    }

    private static class DummyNetworkManager extends NetworkManager{

        DummyNetworkManager(){
            super(EnumProtocolDirection.SERVERBOUND);
            updateFields();
        }

        private void updateFields() {
            try {
                Field channelField = NetworkManager.class.getDeclaredField("channel");
                channelField.setAccessible(true);
                channelField.set(this, new DummyChannel());
                channelField.setAccessible(false);

                Field socketAddressField = NetworkManager.class.getDeclaredField("l");
                socketAddressField.setAccessible(true);
                socketAddressField.set(this, null);
            }
            catch (NoSuchFieldException|SecurityException|IllegalArgumentException|IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

    private static class DummyPlayerConnection extends PlayerConnection {

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
