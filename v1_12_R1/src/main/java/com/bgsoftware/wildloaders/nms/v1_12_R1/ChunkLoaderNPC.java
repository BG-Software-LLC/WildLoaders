package com.bgsoftware.wildloaders.nms.v1_12_R1;

import com.bgsoftware.wildloaders.handlers.NPCHandler;
import com.bgsoftware.wildloaders.npc.DummyChannel;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_12_R1.Advancement;
import net.minecraft.server.v1_12_R1.AdvancementDataPlayer;
import net.minecraft.server.v1_12_R1.AdvancementProgress;
import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.DamageSource;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumGamemode;
import net.minecraft.server.v1_12_R1.EnumProtocolDirection;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.NetworkManager;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayInBlockDig;
import net.minecraft.server.v1_12_R1.PacketPlayInBlockPlace;
import net.minecraft.server.v1_12_R1.PacketPlayInChat;
import net.minecraft.server.v1_12_R1.PacketPlayInFlying;
import net.minecraft.server.v1_12_R1.PacketPlayInHeldItemSlot;
import net.minecraft.server.v1_12_R1.PacketPlayInTransaction;
import net.minecraft.server.v1_12_R1.PacketPlayInUpdateSign;
import net.minecraft.server.v1_12_R1.PacketPlayInWindowClick;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.PlayerInteractManager;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Field;
import java.util.UUID;

public final class ChunkLoaderNPC extends EntityPlayer implements com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC {

    private final AxisAlignedBB boundingBox;
    private final AdvancementDataPlayer advancements;

    public ChunkLoaderNPC(Location location, UUID uuid) {
        super(((CraftServer) Bukkit.getServer()).getServer(),
                ((CraftWorld) location.getWorld()).getHandle(),
                new GameProfile(uuid, NPCHandler.getName(location.getWorld().getName())),
                new PlayerInteractManager(((CraftWorld) location.getWorld()).getHandle()));

        this.boundingBox = new AxisAlignedBB(new BlockPosition(location.getX(), location.getY(), location.getZ()));

        this.playerConnection = new DummyPlayerConnection(server, this);
        this.advancements = new DummyPlayerAdvancements(server, this);

        this.playerInteractManager.setGameMode(EnumGamemode.CREATIVE);
        fauxSleeping = true;

        spawnIn(world);
        setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        world.players.add(this);
        ((WorldServer) world).getPlayerChunkMap().addPlayer(this);

        super.a(this.boundingBox);
    }

    @Override
    public UUID getUniqueId() {
        return super.getUniqueID();
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        return this.boundingBox;
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
    public Player getPlayer() {
        return getBukkitEntity();
    }

    @Override
    protected boolean damageEntity0(DamageSource damagesource, float f) {
        return false;
    }

    @Override
    public AdvancementDataPlayer getAdvancementData() {
        return this.advancements;
    }

    public static class DummyNetworkManager extends NetworkManager {

        private static Field channelField;
        private static Field socketAddressField;

        static {
            try {
                channelField = NetworkManager.class.getDeclaredField("channel");
                channelField.setAccessible(true);
                socketAddressField = NetworkManager.class.getDeclaredField("l");
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

    private static class DummyPlayerAdvancements extends AdvancementDataPlayer {

        DummyPlayerAdvancements(MinecraftServer server, EntityPlayer entityPlayer) {
            super(server, getAdvancementsFile(server, entityPlayer), entityPlayer);
        }

        private static File getAdvancementsFile(MinecraftServer server, EntityPlayer entityPlayer) {
            File advancementsDir = new File(server.getWorldServer(0).getDataManager().getDirectory(), "advancements");
            return new File(advancementsDir, entityPlayer.getUniqueID() + ".json");
        }

        @Override
        public void a(EntityPlayer owner) {
            // setPlayer
        }

        @Override
        public void a() {
            // stopListening
        }

        @Override
        public void b() {
            // reload
        }

        @Override
        public void c() {
            // save
        }

        @Override
        public boolean grantCriteria(Advancement advancement, String criterionName) {
            return false;
        }

        @Override
        public boolean revokeCritera(Advancement advancement, String criterionName) {
            return false;
        }

        @Override
        public void b(EntityPlayer player) {
            // flushDirty
        }

        @Override
        public void a(Advancement advancement) {
            // setSelectedTab
        }

        @Override
        public AdvancementProgress getProgress(Advancement advancement) {
            return new AdvancementProgress();
        }

    }

}
