package com.bgsoftware.wildloaders.nms.v1_19;

import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.loaders.ITileEntityChunkLoader;
import com.bgsoftware.wildloaders.nms.NMSAdapter;
import com.bgsoftware.wildloaders.nms.v1_19.loader.ChunkLoaderBlockEntity;
import com.bgsoftware.wildloaders.scheduler.Scheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;

import javax.annotation.Nullable;
import java.util.UUID;

public final class NMSAdapterImpl implements NMSAdapter {

    @Override
    public String getTag(org.bukkit.inventory.ItemStack bukkitItem, String key, String def) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);
        CompoundTag compoundTag = itemStack.getTag();
        return compoundTag == null || !compoundTag.contains(key, 8) ? def : compoundTag.getString(key);
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack bukkitItem, String key, String value) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);
        CompoundTag compoundTag = itemStack.getOrCreateTag();

        compoundTag.putString(key, value);

        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public long getTag(org.bukkit.inventory.ItemStack bukkitItem, String key, long def) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);
        CompoundTag compoundTag = itemStack.getTag();
        return compoundTag == null || !compoundTag.contains(key, 4) ? def : compoundTag.getLong(key);
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack bukkitItem, String key, long value) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);
        CompoundTag compoundTag = itemStack.getOrCreateTag();

        compoundTag.putLong(key, value);

        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public org.bukkit.inventory.ItemStack getPlayerSkull(org.bukkit.inventory.ItemStack bukkitItem, String texture) {
        ItemStack itemStack = CraftItemStack.asNMSCopy(bukkitItem);
        CompoundTag compoundTag = itemStack.getOrCreateTag();

        CompoundTag skullOwner = compoundTag.contains("SkullOwner") ?
                compoundTag.getCompound("SkullOwner") : new CompoundTag();

        CompoundTag properties = new CompoundTag();
        ListTag textures = new ListTag();
        CompoundTag signature = new CompoundTag();

        signature.putString("Value", texture);
        textures.add(signature);

        properties.put("textures", textures);

        skullOwner.put("Properties", properties);
        skullOwner.putString("Id", UUID.randomUUID().toString());

        compoundTag.put("SkullOwner", skullOwner);

        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC createNPC(Location location, UUID uuid) {
        return new ChunkLoaderNPCImpl(((CraftServer) Bukkit.getServer()).getServer(), location, uuid);
    }

    @Override
    public ITileEntityChunkLoader createLoader(ChunkLoader chunkLoader,
                                               @Nullable OnSpawnerChangeCallback onSpawnerChangeCallback) {
        Location loaderLoc = chunkLoader.getLocation();
        World bukkitWorld = loaderLoc.getWorld();

        if (bukkitWorld == null)
            throw new IllegalArgumentException("Cannot create loader in null world.");

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        BlockPos blockPos = new BlockPos(loaderLoc.getBlockX(), loaderLoc.getBlockY(), loaderLoc.getBlockZ());

        ChunkLoaderBlockEntity ChunkLoaderBlockEntity = new ChunkLoaderBlockEntity(chunkLoader, serverLevel, blockPos);
        serverLevel.addBlockEntityTicker(ChunkLoaderBlockEntity.getTicker());

        if (Scheduler.isRegionScheduler()) {
            Scheduler.runTask(() ->
                    setChunksForcedForLoader(chunkLoader, serverLevel, true, onSpawnerChangeCallback));
        } else {
            setChunksForcedForLoader(chunkLoader, serverLevel, true, onSpawnerChangeCallback);
        }

        return ChunkLoaderBlockEntity;
    }

    @Override
    public void removeLoader(ChunkLoader chunkLoader, boolean spawnParticle,
                             @Nullable OnSpawnerChangeCallback onSpawnerChangeCallback) {
        Location loaderLoc = chunkLoader.getLocation();
        World bukkitWorld = loaderLoc.getWorld();

        if (bukkitWorld == null)
            throw new IllegalArgumentException("Cannot remove loader in null world.");

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        BlockPos blockPos = new BlockPos(loaderLoc.getBlockX(), loaderLoc.getBlockY(), loaderLoc.getBlockZ());

        long chunkPosLong = ChunkPos.asLong(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        ChunkLoaderBlockEntity chunkLoaderBlockEntity = ChunkLoaderBlockEntity.chunkLoaderBlockEntityMap.remove(chunkPosLong);

        if (chunkLoaderBlockEntity != null) {
            chunkLoaderBlockEntity.holograms.values().forEach(EntityHologram::removeHologram);
            chunkLoaderBlockEntity.removed = true;
        }

        if (spawnParticle)
            serverLevel.levelEvent(null, 2001, blockPos, Block.getId(serverLevel.getBlockState(blockPos)));

        if (Scheduler.isRegionScheduler()) {
            Scheduler.runTask(() ->
                    setChunksForcedForLoader(chunkLoader, serverLevel, false, onSpawnerChangeCallback));
        } else {
            setChunksForcedForLoader(chunkLoader, serverLevel, false, onSpawnerChangeCallback);
        }
    }

    private static void setChunksForcedForLoader(ChunkLoader chunkLoader, ServerLevel serverLevel, boolean forced,
                                                 @Nullable OnSpawnerChangeCallback onSpawnerChangeCallback) {
        World bukkitWorld = serverLevel.getWorld();

        int requiredPlayerRange = forced ? -1 : 16;

        for (org.bukkit.Chunk bukkitChunk : chunkLoader.getLoadedChunksCollection()) {
            LevelChunk levelChunk = serverLevel.getChunk(bukkitChunk.getX(), bukkitChunk.getZ());

            for (BlockEntity blockEntity : levelChunk.getBlockEntities().values()) {
                if (blockEntity instanceof SpawnerBlockEntity spawnerBlockEntity) {
                    spawnerBlockEntity.getSpawner().requiredPlayerRange = requiredPlayerRange;
                    if (onSpawnerChangeCallback != null) {
                        BlockPos blockPos = blockEntity.getBlockPos();
                        Location location = new Location(bukkitWorld, blockPos.getX(), blockPos.getY(), blockPos.getZ());
                        onSpawnerChangeCallback.apply(location, requiredPlayerRange);
                    }
                }
            }

            ChunkPos chunkPos = levelChunk.getPos();
            serverLevel.setChunkForced(chunkPos.x, chunkPos.z, forced);
        }
    }

    @Override
    public void updateSpawner(Location location, boolean reset,
                              @Nullable OnSpawnerChangeCallback onSpawnerChangeCallback) {
        World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            throw new IllegalArgumentException("Cannot remove loader in null world.");

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        BlockPos blockPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
        if (!(blockEntity instanceof SpawnerBlockEntity spawnerBlockEntity))
            return;

        int requiredPlayerRange = reset ? 16 : -1;
        spawnerBlockEntity.getSpawner().requiredPlayerRange = requiredPlayerRange;
        if (onSpawnerChangeCallback != null)
            onSpawnerChangeCallback.apply(location, requiredPlayerRange);
    }

}
