package com.bgsoftware.wildloaders.nms.v1_20_4;

import com.bgsoftware.wildloaders.handlers.NPCHandler;
import com.bgsoftware.wildloaders.npc.DummyChannel;
import com.mojang.authlib.GameProfile;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
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
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

public class ChunkLoaderNPCImpl extends com.bgsoftware.wildloaders.nms.v1_20_4.AbstractChunkLoaderNPC {

    private NPCServerPlayer serverPlayer;

    public ChunkLoaderNPCImpl(Location location, UUID uuid) {
        super(location, uuid);
    }

    @Override
    protected ServerPlayer createServerPlayer(ServerLevel serverLevel, UUID uuid) {
        return (this.serverPlayer = new NPCServerPlayer(serverLevel, uuid));
    }

    @Override
    protected ServerGamePacketListenerImpl createDummyServerGamePacketListenerImpl() {
        return new DummyServerGamePacketListenerImpl(MinecraftServer.getServer(), this.serverPlayer);
    }

    @Override
    protected void setViewDistanceInternal(int viewDistance) {
        this.serverPlayer.setLoadViewDistance(viewDistance);
        this.serverPlayer.setTickViewDistance(viewDistance);
        this.serverPlayer.setSendViewDistance(viewDistance);
    }

    @Override
    protected void movePlayerInternal(double x, double y, double z, float yaw, float pitch) {
        this.serverPlayer.moveTo(x, y, z, yaw, pitch);
    }

    @Override
    protected void removePlayerInternal(Entity.RemovalReason removalReason) {
        this.serverPlayer.removeInternal(removalReason);
    }

    private class NPCServerPlayer extends ServerPlayer {

        private final PlayerAdvancements advancements = new DummyPlayerAdvancements(MinecraftServer.getServer(), this);

        NPCServerPlayer(ServerLevel serverLevel, UUID uuid) {
            super(MinecraftServer.getServer(), serverLevel,
                    new GameProfile(uuid, NPCHandler.getName(serverLevel.getWorld().getName())),
                    ClientInformation.createDefault());
        }

        @Override
        public @NotNull AABB getBoundingBoxForCulling() {
            return ChunkLoaderNPCImpl.this.getBoundingBox();
        }

        @Override
        public void remove(@NotNull RemovalReason reason) {
            ChunkLoaderNPCImpl.this.handlePlayerRemove(reason);
        }

        @Override
        public @NotNull PlayerAdvancements getAdvancements() {
            return this.advancements;
        }

        private void removeInternal(RemovalReason reason) {
            super.remove(reason);
        }

    }

    public static class DummyConnection extends Connection {

        DummyConnection() {
            super(PacketFlow.SERVERBOUND);
            this.channel = new DummyChannel();
            this.address = null;
        }

        @Override
        public void setListenerForServerboundHandshake(PacketListener packetListener) {
            // Do nothing - https://github.com/BG-Software-LLC/WildLoaders/issues/148
        }

    }

    public static class DummyServerGamePacketListenerImpl extends ServerGamePacketListenerImpl {

        DummyServerGamePacketListenerImpl(MinecraftServer minecraftServer, ServerPlayer serverPlayer) {
            super(minecraftServer, new DummyConnection(), serverPlayer,
                    CommonListenerCookie.createInitial(serverPlayer.getGameProfile(), false));
        }

        @Override
        public void handleContainerClick(@NotNull ServerboundContainerClickPacket containerClickPacket) {
            // Do nothing.
        }

        @Override
        public void handleMovePlayer(@NotNull ServerboundMovePlayerPacket movePlayerPacket) {
            // Do nothing.
        }

        @Override
        public void handleSignUpdate(@NotNull ServerboundSignUpdatePacket signUpdatePacket) {
            // Do nothing.
        }

        @Override
        public void handlePlayerAction(@NotNull ServerboundPlayerActionPacket playerActionPacket) {
            // Do nothing.
        }

        @Override
        public void handleUseItem(@NotNull ServerboundUseItemPacket useItemPacket) {
            // Do nothing.
        }

        @Override
        public void handleSetCarriedItem(@NotNull ServerboundSetCarriedItemPacket setCarriedItemPacket) {
            // Do nothing.
        }

        @Override
        public void handleChat(@NotNull ServerboundChatPacket chatPacket) {
            // Do nothing.
        }

        @Override
        @Deprecated
        public void disconnect(@NotNull String s) {
            // Do nothing.
        }

        public void send(@NotNull Packet<?> packet) {
            // Do nothing.
        }

    }

    private static class DummyPlayerAdvancements extends PlayerAdvancements {

        DummyPlayerAdvancements(MinecraftServer server, ServerPlayer serverPlayer) {
            super(server.getFixerUpper(), server.getPlayerList(), server.getAdvancements(),
                    getAdvancementsFile(server, serverPlayer), serverPlayer);
        }

        private static Path getAdvancementsFile(MinecraftServer server, ServerPlayer serverPlayer) {
            File advancementsDir = server.getWorldPath(LevelResource.PLAYER_ADVANCEMENTS_DIR).toFile();
            return new File(advancementsDir, serverPlayer.getUUID() + ".json").toPath();
        }

        @Override
        public void setPlayer(@NotNull ServerPlayer owner) {
            // Do nothing.
        }

        @Override
        public void stopListening() {
            // Do nothing.
        }

        @Override
        public void reload(@NotNull ServerAdvancementManager advancementLoader) {
            // Do nothing.
        }

        @Override
        public void save() {
            // Do nothing.
        }

        @Override
        public boolean award(@NotNull AdvancementHolder advancement, @NotNull String criterionName) {
            return false;
        }

        @Override
        public boolean revoke(@NotNull AdvancementHolder advancement, @NotNull String criterionName) {
            return false;
        }

        @Override
        public void flushDirty(@NotNull ServerPlayer player) {
            // Do nothing.
        }

        @Override
        public void setSelectedTab(@Nullable AdvancementHolder advancement) {
            // Do nothing.
        }

        @Override
        public @NotNull AdvancementProgress getOrStartProgress(@NotNull AdvancementHolder advancement) {
            return new AdvancementProgress();
        }

    }

}
