package com.bgsoftware.wildloaders.nms.v1_18_R1;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.loaders.ITileEntityChunkLoader;
import com.bgsoftware.wildloaders.nms.mapping.Remap;
import com.bgsoftware.wildloaders.nms.v1_18_R1.loader.TileEntityChunkLoader;
import com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.net.minecraft.core.BlockPosition;
import com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.net.minecraft.nbt.NBTTagCompound;
import com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.net.minecraft.world.item.ItemStack;
import com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.net.minecraft.world.level.ChunkCoordIntPair;
import com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.net.minecraft.world.level.World;
import com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.net.minecraft.world.level.block.entity.TileEntity;
import com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.net.minecraft.world.level.block.state.IBlockData;
import com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.net.minecraft.world.level.chunk.Chunk;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;

import java.util.UUID;

@SuppressWarnings("unused")
public final class NMSAdapter implements com.bgsoftware.wildloaders.nms.NMSAdapter {

    private static final WildLoadersPlugin plugin = WildLoadersPlugin.getPlugin();

    @Remap(classPath = "net.minecraft.world.level.chunk.LevelChunk", name = "createTicker", type = Remap.Type.METHOD)
    private static final ReflectMethod<TickingBlockEntity> CREATE_TICKING_BLOCK = new ReflectMethod<>(
            net.minecraft.world.level.chunk.Chunk.class, "a",
            net.minecraft.world.level.block.entity.TileEntity.class, BlockEntityTicker.class);

    @Override
    public String getTag(org.bukkit.inventory.ItemStack itemStack, String key, String def) {
        ItemStack nmsItem = new ItemStack(CraftItemStack.asNMSCopy(itemStack));
        NBTTagCompound tagCompound = nmsItem.getTag();

        if (tagCompound == null || !tagCompound.contains(key, 8))
            return def;

        return tagCompound.getString(key);
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack itemStack, String key, String value) {
        ItemStack nmsItem = new ItemStack(CraftItemStack.asNMSCopy(itemStack));
        NBTTagCompound tagCompound = nmsItem.getOrCreateTag();

        tagCompound.put(key, NBTTagString.a(value));

        return CraftItemStack.asBukkitCopy(nmsItem.getHandle());
    }

    @Override
    public long getTag(org.bukkit.inventory.ItemStack itemStack, String key, long def) {
        ItemStack nmsItem = new ItemStack(CraftItemStack.asNMSCopy(itemStack));
        NBTTagCompound tagCompound = nmsItem.getTag();

        if (tagCompound == null || !tagCompound.contains(key, 4))
            return def;

        return tagCompound.getLong(key);
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack itemStack, String key, long value) {
        ItemStack nmsItem = new ItemStack(CraftItemStack.asNMSCopy(itemStack));
        NBTTagCompound tagCompound = nmsItem.getOrCreateTag();

        tagCompound.put(key, NBTTagLong.a(value));

        return CraftItemStack.asBukkitCopy(nmsItem.getHandle());
    }

    @Override
    public org.bukkit.inventory.ItemStack getPlayerSkull(org.bukkit.inventory.ItemStack itemStack, String texture) {
        ItemStack nmsItem = new ItemStack(CraftItemStack.asNMSCopy(itemStack));
        NBTTagCompound tagCompound = nmsItem.getOrCreateTag();

        NBTTagCompound skullOwner = tagCompound.contains("SkullOwner") ?
                tagCompound.getCompound("SkullOwner") : new NBTTagCompound();

        NBTTagCompound properties = new NBTTagCompound();

        NBTTagList textures = new NBTTagList();
        NBTTagCompound signature = new NBTTagCompound();
        signature.putString("Value", texture);
        textures.add(signature.getHandle());

        properties.put("textures", textures);

        skullOwner.put("Properties", properties.getHandle());
        skullOwner.putString("Id", UUID.randomUUID().toString());

        tagCompound.put("SkullOwner", skullOwner.getHandle());

        return CraftItemStack.asBukkitCopy(nmsItem.getHandle());
    }

    @Override
    public com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC createNPC(Location location, UUID uuid) {
        return new ChunkLoaderNPC(((CraftServer) Bukkit.getServer()).getServer(), location, uuid);
    }

    @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
            name = "requiredPlayerRange",
            type = Remap.Type.FIELD,
            remappedName = "m")
    @Override
    public ITileEntityChunkLoader createLoader(ChunkLoader chunkLoader) {
        Location loaderLoc = chunkLoader.getLocation();
        assert loaderLoc.getWorld() != null;

        World world = new World(((CraftWorld) loaderLoc.getWorld()).getHandle());
        BlockPosition blockPosition = new BlockPosition(loaderLoc.getX(), loaderLoc.getY(), loaderLoc.getZ());

        TileEntityChunkLoader tileEntityChunkLoader = new TileEntityChunkLoader(chunkLoader, world, blockPosition);
        world.addBlockEntityTicker(tileEntityChunkLoader.ticker);

        for (org.bukkit.Chunk bukkitChunk : chunkLoader.getLoadedChunks()) {
            Chunk chunk = new Chunk(((CraftChunk) bukkitChunk).getHandle());
            chunk.getBlockEntities().values().stream()
                    .filter(nmsTileEntity -> nmsTileEntity instanceof TileEntityMobSpawner)
                    .forEach(nmsTileEntity -> {
                        TileEntity tileEntity = new TileEntity(nmsTileEntity);
                        tileEntity.getSpawner().m = -1;
                    });

            ChunkCoordIntPair chunkCoords = chunk.getPos();

            world.setChunkForced(chunkCoords.getX(), chunkCoords.getZ(), true);
        }

        return tileEntityChunkLoader;
    }

    @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
            name = "requiredPlayerRange",
            type = Remap.Type.FIELD,
            remappedName = "m")
    @Override
    public void removeLoader(ChunkLoader chunkLoader, boolean spawnParticle) {
        Location loaderLoc = chunkLoader.getLocation();
        assert loaderLoc.getWorld() != null;

        World world = new World(((CraftWorld) loaderLoc.getWorld()).getHandle());
        BlockPosition blockPosition = new BlockPosition(loaderLoc.getX(), loaderLoc.getY(), loaderLoc.getZ());

        long tileEntityLong = ChunkCoordIntPair.asLong(blockPosition.getX() >> 4, blockPosition.getZ() >> 4);
        TileEntityChunkLoader tileEntityChunkLoader = TileEntityChunkLoader.tileEntityChunkLoaderMap.remove(tileEntityLong);
        if (tileEntityChunkLoader != null) {
            tileEntityChunkLoader.holograms.forEach(EntityHolograms::removeHologram);
            tileEntityChunkLoader.removed = true;
        }

        if (spawnParticle) {
            world.levelEvent(null, 2001, blockPosition.getHandle(),
                    IBlockData.getId(world.getBlockStateNoMappings(blockPosition.getHandle())));
        }

        for (org.bukkit.Chunk bukkitChunk : chunkLoader.getLoadedChunks()) {
            Chunk chunk = new Chunk(((CraftChunk) bukkitChunk).getHandle());
            chunk.getBlockEntities().values().stream()
                    .filter(nmsTileEntity -> nmsTileEntity instanceof TileEntityMobSpawner)
                    .forEach(nmsTileEntity -> {
                        TileEntity tileEntity = new TileEntity(nmsTileEntity);
                        tileEntity.getSpawner().m = 16;
                    });

            ChunkCoordIntPair chunkCoords = chunk.getPos();

            world.setChunkForced(chunkCoords.getX(), chunkCoords.getZ(), false);
        }
    }

    @Remap(classPath = "net.minecraft.world.level.BaseSpawner",
            name = "requiredPlayerRange",
            type = Remap.Type.FIELD,
            remappedName = "m")
    @Override
    public void updateSpawner(Location location, boolean reset) {
        assert location.getWorld() != null;
        World world = new World(((CraftWorld) location.getWorld()).getHandle());

        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        TileEntity mobSpawner = new TileEntity(world.getBlockEntity(blockPosition.getHandle()));

        mobSpawner.getSpawner().m = reset ? 16 : -1;
    }

}
