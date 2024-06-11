package com.bgsoftware.wildloaders.nms.v1_7_R4;

import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.loaders.ITileEntityChunkLoader;
import com.bgsoftware.wildloaders.nms.NMSAdapter;
import com.bgsoftware.wildloaders.nms.v1_7_R4.loader.TileEntityChunkLoader;
import com.bgsoftware.wildloaders.scheduler.Scheduler;
import net.minecraft.server.v1_7_R4.Block;
import net.minecraft.server.v1_7_R4.Chunk;
import net.minecraft.server.v1_7_R4.ItemStack;
import net.minecraft.server.v1_7_R4.NBTTagCompound;
import net.minecraft.server.v1_7_R4.NBTTagList;
import net.minecraft.server.v1_7_R4.NBTTagLong;
import net.minecraft.server.v1_7_R4.NBTTagString;
import net.minecraft.server.v1_7_R4.TileEntity;
import net.minecraft.server.v1_7_R4.TileEntityMobSpawner;
import net.minecraft.server.v1_7_R4.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_7_R4.CraftChunk;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_7_R4.util.LongHash;

import java.util.Collection;
import java.util.UUID;

public final class NMSAdapterImpl implements NMSAdapter {

    @Override
    public String getTag(org.bukkit.inventory.ItemStack itemStack, String key, String def) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        if (!tagCompound.hasKeyOfType(key, 8))
            return def;

        return tagCompound.getString(key);
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack itemStack, String key, String value) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        tagCompound.set(key, new NBTTagString(value));

        nmsItem.setTag(tagCompound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public long getTag(org.bukkit.inventory.ItemStack itemStack, String key, long def) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        if (!tagCompound.hasKeyOfType(key, 4))
            return def;

        return tagCompound.getLong(key);
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack itemStack, String key, long value) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        tagCompound.set(key, new NBTTagLong(value));

        nmsItem.setTag(tagCompound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public org.bukkit.inventory.ItemStack getPlayerSkull(org.bukkit.inventory.ItemStack itemStack, String texture) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        NBTTagCompound nbtTagCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

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
    public ITileEntityChunkLoader createLoader(ChunkLoader chunkLoader) {
        Location loaderLoc = chunkLoader.getLocation();
        World world = ((CraftWorld) loaderLoc.getWorld()).getHandle();
        int x = loaderLoc.getBlockX(), y = loaderLoc.getBlockY(), z = loaderLoc.getBlockZ();

        TileEntityChunkLoader tileEntityChunkLoader = new TileEntityChunkLoader(chunkLoader, world, x, y, z);
        //noinspection unchecked
        world.tileEntityList.add(tileEntityChunkLoader);

        if (Scheduler.isRegionScheduler()) {
            Scheduler.runTask(() -> setSpawnersRangeForLoader(chunkLoader, true));
        } else {
            setSpawnersRangeForLoader(chunkLoader, true);
        }

        return tileEntityChunkLoader;
    }

    @Override
    public void removeLoader(ChunkLoader chunkLoader, boolean spawnParticle) {
        Location loaderLoc = chunkLoader.getLocation();
        World world = ((CraftWorld) loaderLoc.getWorld()).getHandle();
        int x = loaderLoc.getBlockX(), y = loaderLoc.getBlockY(), z = loaderLoc.getBlockZ();

        long tileEntityLong = LongHash.toLong(x >> 4, z >> 4);
        TileEntityChunkLoader tileEntityChunkLoader = TileEntityChunkLoader.tileEntityChunkLoaderMap.remove(tileEntityLong);
        if (tileEntityChunkLoader != null) {
            tileEntityChunkLoader.removed = true;
            world.tileEntityList.remove(tileEntityChunkLoader);
        }

        if (spawnParticle)
            world.a(null, 2001, x, y, z, Block.getId(world.getType(x, y, z)) + (world.getData(x, y, z) << 12));

        if (Scheduler.isRegionScheduler()) {
            Scheduler.runTask(() -> setSpawnersRangeForLoader(chunkLoader, false));
        } else {
            setSpawnersRangeForLoader(chunkLoader, false);
        }
    }

    private static void setSpawnersRangeForLoader(ChunkLoader chunkLoader, boolean loaded) {
        short requiredPlayerRange = (short) (loaded ? -1 : 16);
        for (org.bukkit.Chunk bukkitChunk : chunkLoader.getLoadedChunksCollection()) {
            Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();

            for (TileEntity tileEntity : (Collection<TileEntity>) chunk.tileEntities.values()) {
                if (tileEntity instanceof TileEntityMobSpawner) {
                    NBTTagCompound nbtTagCompound = new NBTTagCompound();
                    tileEntity.b(nbtTagCompound);
                    nbtTagCompound.setShort("RequiredPlayerRange", requiredPlayerRange);
                    tileEntity.a(nbtTagCompound);
                }
            }
        }
    }

    @Override
    public void updateSpawner(Location location, boolean reset) {
        World world = ((CraftWorld) location.getWorld()).getHandle();

        TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner)
                world.getTileEntity(location.getBlockX(), location.getBlockY(), location.getBlockZ());

        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        mobSpawner.b(nbtTagCompound);
        nbtTagCompound.setShort("RequiredPlayerRange", (short) (reset ? 16 : -1));
        mobSpawner.a(nbtTagCompound);
    }

}
