package com.bgsoftware.wildloaders.nms.v119;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC;
import com.bgsoftware.wildloaders.handlers.NPCHandler;
import com.bgsoftware.wildloaders.npc.DummyChannel;
import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.UUID;

public final class ChunkLoaderNPCImpl extends ServerPlayer implements ChunkLoaderNPC {

    private static final ReflectMethod<Void> SET_GAMEMODE = new ReflectMethod<>(ServerPlayerGameMode.class,
            1, GameType.class, GameType.class);

    private final ServerLevel serverLevel;
    private final AABB boundingBox;
    private final PlayerAdvancements advancements;

    private boolean dieCall = false;

    public ChunkLoaderNPCImpl(MinecraftServer minecraftServer, Location location, UUID uuid) {
        super(minecraftServer, ((CraftWorld) location.getWorld()).getHandle(),
                new GameProfile(uuid, NPCHandler.getName(location.getWorld().getName())), null);

        this.serverLevel = getLevel();
        this.boundingBox = new AABB(new BlockPos(location.getX(), location.getY(), location.getZ()));

        this.connection = new DummyServerGamePacketListenerImpl(minecraftServer, this);
        this.advancements = new DummyPlayerAdvancements(minecraftServer, this);

        SET_GAMEMODE.invoke(this.gameMode, GameType.CREATIVE, null);
        clientViewDistance = 1;

        fauxSleeping = true;

        spawnIn(this.serverLevel);
        moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

        this.serverLevel.addNewPlayer(this);

        super.setBoundingBox(this.boundingBox);
    }

    @Override
    public UUID getUniqueId() {
        return super.getUUID();
    }

    @Override
    public void die() {
        discard();
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.boundingBox;
    }

    @Override
    public void remove(RemovalReason removalReason) {
        if (!dieCall) {
            dieCall = true;
            this.serverLevel.removePlayerImmediately(this, RemovalReason.UNLOADED_WITH_PLAYER);
            dieCall = false;
        } else {
            super.remove(removalReason);
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
    public PlayerAdvancements getAdvancements() {
        return advancements;
    }

    public static class DummyConnection extends Connection {

        DummyConnection() {
            super(PacketFlow.SERVERBOUND);
            this.channel = new DummyChannel();
            this.address = null;
        }

    }

    public static class DummyServerGamePacketListenerImpl extends ServerGamePacketListenerImpl {

        DummyServerGamePacketListenerImpl(MinecraftServer minecraftServer, ServerPlayer serverPlayer) {
            super(minecraftServer, new DummyConnection(), serverPlayer);
        }

        @Override
        public void handleContainerClick(ServerboundContainerClickPacket containerClickPacket) {
            // Do nothing.
        }

        @Override
        public void handleMovePlayer(ServerboundMovePlayerPacket movePlayerPacket) {
            // Do nothing.
        }

        @Override
        public void handleSignUpdate(ServerboundSignUpdatePacket signUpdatePacket) {
            // Do nothing.
        }

        @Override
        public void handlePlayerAction(ServerboundPlayerActionPacket playerActionPacket) {
            // Do nothing.
        }

        @Override
        public void handleUseItem(ServerboundUseItemPacket useItemPacket) {
            // Do nothing.
        }

        @Override
        public void handleSetCarriedItem(ServerboundSetCarriedItemPacket setCarriedItemPacket) {
            // Do nothing.
        }

        @Override
        public void handleChat(ServerboundChatPacket chatPacket) {
            // Do nothing.
        }

        @Override
        public void disconnect(String s) {

        }

        public void send(Packet<?> packet) {
            // Do nothing.
        }

    }

    private static class DummyPlayerAdvancements extends PlayerAdvancements {

        DummyPlayerAdvancements(MinecraftServer server, ServerPlayer serverPlayer) {
            super(server.getFixerUpper(), server.getPlayerList(), server.getAdvancements(),
                    getAdvancementsFile(server, serverPlayer), serverPlayer);
        }

        private static File getAdvancementsFile(MinecraftServer server, ServerPlayer serverPlayer) {
            File advancementsDir = server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).toFile();
            return new File(advancementsDir, serverPlayer.getUUID() + ".json");
        }

        @Override
        public void setPlayer(ServerPlayer owner) {
            // Do nothing.
        }

        @Override
        public void stopListening() {
            // Do nothing.
        }

        @Override
        public void reload(ServerAdvancementManager advancementLoader) {
            // Do nothing.
        }

        @Override
        public void save() {
            // Do nothing.
        }

        @Override
        public boolean award(Advancement advancement, String criterionName) {
            return false;
        }

        @Override
        public boolean revoke(Advancement advancement, String criterionName) {
            return false;
        }

        @Override
        public void flushDirty(ServerPlayer player) {
            // Do nothing.
        }

        @Override
        public void setSelectedTab(@Nullable Advancement advancement) {
            // Do nothing.
        }

        @Override
        public AdvancementProgress getOrStartProgress(Advancement advancement) {
            return new AdvancementProgress();
        }

    }

}
