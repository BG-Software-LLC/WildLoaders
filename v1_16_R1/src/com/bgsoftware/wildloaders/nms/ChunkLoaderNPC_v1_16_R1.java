package com.bgsoftware.wildloaders.nms;

import com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC;
import com.bgsoftware.wildloaders.npc.DummyChannel;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R1.EntityPlayer;
import net.minecraft.server.v1_16_R1.EnumGamemode;
import net.minecraft.server.v1_16_R1.EnumProtocolDirection;
import net.minecraft.server.v1_16_R1.MinecraftServer;
import net.minecraft.server.v1_16_R1.NetworkManager;
import net.minecraft.server.v1_16_R1.Packet;
import net.minecraft.server.v1_16_R1.PacketPlayInBlockDig;
import net.minecraft.server.v1_16_R1.PacketPlayInBlockPlace;
import net.minecraft.server.v1_16_R1.PacketPlayInChat;
import net.minecraft.server.v1_16_R1.PacketPlayInFlying;
import net.minecraft.server.v1_16_R1.PacketPlayInHeldItemSlot;
import net.minecraft.server.v1_16_R1.PacketPlayInTransaction;
import net.minecraft.server.v1_16_R1.PacketPlayInUpdateSign;
import net.minecraft.server.v1_16_R1.PacketPlayInWindowClick;
import net.minecraft.server.v1_16_R1.PlayerConnection;
import net.minecraft.server.v1_16_R1.PlayerInteractManager;
import net.minecraft.server.v1_16_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;

import java.lang.reflect.Field;
import java.util.UUID;

public final class ChunkLoaderNPC_v1_16_R1 extends EntityPlayer implements ChunkLoaderNPC {

    private boolean dieCall = false;

    public ChunkLoaderNPC_v1_16_R1(Location location, UUID uuid){
        super(((CraftServer) Bukkit.getServer()).getServer(), ((CraftWorld) location.getWorld()).getHandle(),
                new GameProfile(uuid, "Loader-" + location.getWorld().getName()), new PlayerInteractManager(((CraftWorld) location.getWorld()).getHandle()));

        playerConnection = new DummyPlayerConnection(server, this);

        a(EnumGamemode.CREATIVE);
        clientViewDistance = 1;

        fauxSleeping = true;

        spawnIn(world);
        setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        ((WorldServer) world).addPlayerJoin(this);
    }

    @Override
    public UUID getUniqueId() {
        return super.getUniqueID();
    }

    @Override
    public void die() {
        if(!dieCall) {
            dieCall = true;
            getWorldServer().removePlayer(this);
            dieCall = false;
        }
        else {
            super.die();
        }
    }

    @Override
    public Location getLocation() {
        return getBukkitEntity().getLocation();
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

                Field socketAddressField = NetworkManager.class.getDeclaredField("socketAddress");
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
