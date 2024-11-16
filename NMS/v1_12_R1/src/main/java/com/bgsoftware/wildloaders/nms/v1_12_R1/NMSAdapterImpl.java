package com.bgsoftware.wildloaders.nms.v1_12_R1;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.loaders.ITileEntityChunkLoader;
import com.bgsoftware.wildloaders.nms.NMSAdapter;
import com.bgsoftware.wildloaders.nms.v1_12_R1.loader.TileEntityChunkLoader;
import com.bgsoftware.wildloaders.scheduler.Scheduler;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagList;
import net.minecraft.server.v1_12_R1.NBTTagLong;
import net.minecraft.server.v1_12_R1.NBTTagString;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_12_R1.World;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_12_R1.util.LongHash;

import javax.annotation.Nullable;
import java.util.UUID;

public final class NMSAdapterImpl implements NMSAdapter {

    private static final ReflectMethod<Void> TILE_ENTITY_LOAD = new ReflectMethod<>(TileEntity.class, "load", NBTTagCompound.class);

    @Override
    public String getTag(org.bukkit.inventory.ItemStack itemStack, String key, String def) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.getTag();
        return tagCompound == null || !tagCompound.hasKeyOfType(key, 8) ? def : tagCompound.getString(key);
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack itemStack, String key, String value) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        assert tagCompound != null;

        tagCompound.set(key, new NBTTagString(value));

        nmsItem.setTag(tagCompound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public long getTag(org.bukkit.inventory.ItemStack itemStack, String key, long def) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.getTag();
        return tagCompound == null || !tagCompound.hasKeyOfType(key, 4) ? def : tagCompound.getLong(key);
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack itemStack, String key, long value) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        assert tagCompound != null;

        tagCompound.set(key, new NBTTagLong(value));

        nmsItem.setTag(tagCompound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public org.bukkit.inventory.ItemStack getPlayerSkull(org.bukkit.inventory.ItemStack itemStack, String texture) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        NBTTagCompound nbtTagCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        assert nbtTagCompound != null;

        NBTTagCompound skullOwner = nbtTagCompound.hasKey("SkullOwner") ? nbtTagCompound.getCompound("SkullOwner") : new NBTTagCompound();

        NBTTagCompound properties = new NBTTagCompound();

        NBTTagList textures = new NBTTagList();
        NBTTagCompound signature = new NBTTagCompound();
        signature.setString("Value", texture);
        textures.add(signature);

        properties.set("textures", textures);

        skullOwner.set("Properties", properties);
        skullOwner.setString("Id", UUID.randomUUID().toString());

        nbtTagCompound.set("SkullOwner", skullOwner);

        nmsItem.setTag(nbtTagCompound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC createNPC(Location location, UUID uuid) {
        return new ChunkLoaderNPC(location, uuid);
    }

    @Override
    public ITileEntityChunkLoader createLoader(ChunkLoader chunkLoader,
                                               @Nullable OnSpawnerChangeCallback onSpawnerChangeCallback) {
        Location loaderLoc = chunkLoader.getLocation();
        WorldServer worldServer = ((CraftWorld) loaderLoc.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(loaderLoc.getX(), loaderLoc.getY(), loaderLoc.getZ());

        TileEntityChunkLoader tileEntityChunkLoader = new TileEntityChunkLoader(chunkLoader, worldServer, blockPosition);
        worldServer.tileEntityListTick.add(tileEntityChunkLoader);

        if (Scheduler.isRegionScheduler()) {
            Scheduler.runTask(() ->
                    setSpawnersRangeForLoader(chunkLoader, worldServer, true, onSpawnerChangeCallback));
        } else {
            setSpawnersRangeForLoader(chunkLoader, worldServer, true, onSpawnerChangeCallback);
        }

        return tileEntityChunkLoader;
    }

    @Override
    public void removeLoader(ChunkLoader chunkLoader, boolean spawnParticle,
                             @Nullable OnSpawnerChangeCallback onSpawnerChangeCallback) {
        Location loaderLoc = chunkLoader.getLocation();
        WorldServer worldServer = ((CraftWorld) loaderLoc.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(loaderLoc.getX(), loaderLoc.getY(), loaderLoc.getZ());

        long tileEntityLong = LongHash.toLong(blockPosition.getX() >> 4, blockPosition.getZ() >> 4);
        TileEntityChunkLoader tileEntityChunkLoader = TileEntityChunkLoader.tileEntityChunkLoaderMap.remove(tileEntityLong);
        if (tileEntityChunkLoader != null) {
            tileEntityChunkLoader.holograms.forEach(EntityHolograms::removeHologram);
            tileEntityChunkLoader.removed = true;
            worldServer.tileEntityListTick.remove(tileEntityChunkLoader);
        }

        if (spawnParticle)
            worldServer.a(null, 2001, blockPosition,
                    Block.getCombinedId(worldServer.getType(blockPosition)));

        if (Scheduler.isRegionScheduler()) {
            Scheduler.runTask(() ->
                    setSpawnersRangeForLoader(chunkLoader, worldServer, false, onSpawnerChangeCallback));
        } else {
            setSpawnersRangeForLoader(chunkLoader, worldServer, false, onSpawnerChangeCallback);
        }
    }

    private static void setSpawnersRangeForLoader(ChunkLoader chunkLoader, WorldServer worldServer, boolean loaded,
                                                  @Nullable OnSpawnerChangeCallback onSpawnerChangeCallback) {
        org.bukkit.World bukkitWorld = worldServer.getWorld();

        short requiredPlayerRange = (short) (loaded ? -1 : 16);

        for (org.bukkit.Chunk bukkitChunk : chunkLoader.getLoadedChunksCollection()) {
            Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();

            for (TileEntity tileEntity : chunk.tileEntities.values()) {
                if (tileEntity instanceof TileEntityMobSpawner) {
                    setSpawnerRange((TileEntityMobSpawner) tileEntity, requiredPlayerRange);
                    if (onSpawnerChangeCallback != null) {
                        BlockPosition blockPosition = tileEntity.getPosition();
                        Location location = new Location(bukkitWorld, blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
                        onSpawnerChangeCallback.apply(location, requiredPlayerRange);
                    }
                }
            }
        }
    }

    @Override
    public void updateSpawner(Location location, boolean reset,
                              @Nullable OnSpawnerChangeCallback onSpawnerChangeCallback) {
        org.bukkit.World bukkitWorld = location.getWorld();

        if (bukkitWorld == null)
            throw new IllegalArgumentException("Cannot remove loader in null world.");

        World world = ((CraftWorld) bukkitWorld).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        TileEntity tileEntity = world.getTileEntity(blockPosition);
        if (!(tileEntity instanceof TileEntityMobSpawner))
            return;

        TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner) tileEntity;

        int requiredPlayerRange = reset ? 16 : -1;
        setSpawnerRange(mobSpawner, requiredPlayerRange);
        if (onSpawnerChangeCallback != null)
            onSpawnerChangeCallback.apply(location, requiredPlayerRange);
    }

    private static void setSpawnerRange(TileEntityMobSpawner mobSpawner, int range) {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        mobSpawner.save(nbtTagCompound);
        nbtTagCompound.setShort("RequiredPlayerRange", (short) range);
        if (TILE_ENTITY_LOAD.isValid()) {
            TILE_ENTITY_LOAD.invoke(mobSpawner, nbtTagCompound);
        } else {
            mobSpawner.a(nbtTagCompound);
        }
    }

}
