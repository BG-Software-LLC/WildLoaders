package com.bgsoftware.wildloaders.nms.v1_16_R3;

import com.bgsoftware.wildloaders.handlers.NPCHandler;
import com.bgsoftware.wildloaders.npc.DummyChannel;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.Advancement;
import net.minecraft.server.v1_16_R3.AdvancementDataPlayer;
import net.minecraft.server.v1_16_R3.AdvancementDataWorld;
import net.minecraft.server.v1_16_R3.AdvancementProgress;
import net.minecraft.server.v1_16_R3.AxisAlignedBB;
import net.minecraft.server.v1_16_R3.BlockPosition;
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
import net.minecraft.server.v1_16_R3.SavedFile;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.UUID;

public final class ChunkLoaderNPC extends EntityPlayer implements com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC {


    private final AxisAlignedBB boundingBox;
    private final AdvancementDataPlayer advancements;

    private boolean dieCall = false;

    public ChunkLoaderNPC(Location location, UUID uuid) {
        super(((CraftServer) Bukkit.getServer()).getServer(),
                ((CraftWorld) location.getWorld()).getHandle(),
                new GameProfile(uuid, NPCHandler.getName(location.getWorld().getName())),
                new PlayerInteractManager(((CraftWorld) location.getWorld()).getHandle()));

        this.boundingBox = new AxisAlignedBB(new BlockPosition(location.getX(), location.getY(), location.getZ()));

        this.playerConnection = new DummyPlayerConnection(server, this);
        this.advancements = new DummyPlayerAdvancements(server, this);

        this.playerInteractManager.setGameMode(EnumGamemode.CREATIVE);

        fallDistance = 0.0F;
        fauxSleeping = true;
        clientViewDistance = 0;

        try {
            // Paper
            affectsSpawning = true;
        } catch (Throwable ignored) {
        }

        spawnIn(world);
        setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        ((WorldServer) world).addPlayerJoin(this);

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

    private static class DummyPlayerAdvancements extends AdvancementDataPlayer {

        DummyPlayerAdvancements(MinecraftServer server, EntityPlayer entityPlayer) {
            super(server.getDataFixer(), server.getPlayerList(), server.getAdvancementData(),
                    getAdvancementsFile(server, entityPlayer), entityPlayer);
        }

        private static File getAdvancementsFile(MinecraftServer server, EntityPlayer entityPlayer) {
            File advancementsDir = server.a(SavedFile.ADVANCEMENTS).toFile();
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
        public void a(AdvancementDataWorld advancementLoader) {
            // reload
        }

        @Override
        public void b() {
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
        public void a(@Nullable Advancement advancement) {
            // setSelectedTab
        }

        @Override
        public AdvancementProgress getProgress(Advancement advancement) {
            return new AdvancementProgress();
        }

    }

}
