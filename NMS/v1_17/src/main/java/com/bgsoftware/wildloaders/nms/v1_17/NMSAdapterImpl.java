package com.bgsoftware.wildloaders.nms.v1_17;

import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.loaders.ITileEntityChunkLoader;
import com.bgsoftware.wildloaders.nms.NMSAdapter;
import com.bgsoftware.wildloaders.nms.v1_17.loader.ChunkLoaderBlockEntity;
import com.bgsoftware.wildloaders.nms.v1_17.npc.ChunkLoaderNPCWrapper;
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
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;

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
        return new ChunkLoaderNPCWrapper(((CraftServer) Bukkit.getServer()).getServer(), location, uuid);
    }

    @Override
    public ITileEntityChunkLoader createLoader(ChunkLoader chunkLoader) {
        Location loaderLoc = chunkLoader.getLocation();
        World bukkitWorld = loaderLoc.getWorld();

        if (bukkitWorld == null)
            throw new IllegalArgumentException("Cannot create loader in null world.");

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        BlockPos blockPos = new BlockPos(loaderLoc.getX(), loaderLoc.getY(), loaderLoc.getZ());

        ChunkLoaderBlockEntity ChunkLoaderBlockEntity = new ChunkLoaderBlockEntity(chunkLoader, serverLevel, blockPos);
        serverLevel.addBlockEntityTicker(ChunkLoaderBlockEntity.getTicker());

        if (Scheduler.isRegionScheduler()) {
            Scheduler.runTask(() -> setChunksForcedForLoader(chunkLoader, serverLevel, true));
        } else {
            setChunksForcedForLoader(chunkLoader, serverLevel, true);
        }

        return ChunkLoaderBlockEntity;
    }

    @Override
    public void removeLoader(ChunkLoader chunkLoader, boolean spawnParticle) {
        Location loaderLoc = chunkLoader.getLocation();
        World bukkitWorld = loaderLoc.getWorld();

        if (bukkitWorld == null)
            throw new IllegalArgumentException("Cannot remove loader in null world.");

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        BlockPos blockPos = new BlockPos(loaderLoc.getX(), loaderLoc.getY(), loaderLoc.getZ());

        long chunkPosLong = ChunkPos.asLong(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        ChunkLoaderBlockEntity chunkLoaderBlockEntity = ChunkLoaderBlockEntity.chunkLoaderBlockEntityMap.remove(chunkPosLong);

        if (chunkLoaderBlockEntity != null) {
            chunkLoaderBlockEntity.holograms.forEach(EntityHologram::removeHologram);
            chunkLoaderBlockEntity.removed = true;
        }

        if (spawnParticle)
            serverLevel.levelEvent(null, 2001, blockPos, Block.getId(serverLevel.getBlockState(blockPos)));

        if (Scheduler.isRegionScheduler()) {
            Scheduler.runTask(() -> setChunksForcedForLoader(chunkLoader, serverLevel, false));
        } else {
            setChunksForcedForLoader(chunkLoader, serverLevel, false);
        }
    }

    private static void setChunksForcedForLoader(ChunkLoader chunkLoader, ServerLevel serverLevel, boolean forced) {
        int requiredPlayerRange = forced ? -1 : 16;
        for (org.bukkit.Chunk bukkitChunk : chunkLoader.getLoadedChunksCollection()) {
            LevelChunk levelChunk = ((CraftChunk) bukkitChunk).getHandle();

            for (BlockEntity blockEntity : levelChunk.getBlockEntities().values()) {
                if (blockEntity instanceof SpawnerBlockEntity spawnerBlockEntity)
                    spawnerBlockEntity.getSpawner().requiredPlayerRange = requiredPlayerRange;
            }

            ChunkPos chunkPos = levelChunk.getPos();
            serverLevel.setChunkForced(chunkPos.x, chunkPos.z, forced);
        }
    }

    @Override
    public void updateSpawner(Location location, boolean reset) {
        World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            throw new IllegalArgumentException("Cannot remove loader in null world.");

        ServerLevel serverLevel = ((CraftWorld) bukkitWorld).getHandle();
        BlockPos blockPos = new BlockPos(location.getX(), location.getY(), location.getZ());
        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
        if (blockEntity instanceof SpawnerBlockEntity spawnerBlockEntity)
            spawnerBlockEntity.getSpawner().requiredPlayerRange = reset ? 16 : -1;
    }

}
