package com.bgsoftware.wildloaders.nms;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.holograms.Hologram;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC;
import com.bgsoftware.wildloaders.loaders.ITileEntityChunkLoader;
import com.bgsoftware.wildloaders.loaders.WChunkLoader;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.Chunk;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class NMSAdapter_v1_17_R1 implements NMSAdapter {

    private static final WildLoadersPlugin plugin = WildLoadersPlugin.getPlugin();

    private static final ReflectMethod<TickingBlockEntity> CREATE_TICKING_BLOCK = new ReflectMethod<>(
            Chunk.class, "a", TileEntity.class, BlockEntityTicker.class);

    @Override
    public String getTag(org.bukkit.inventory.ItemStack itemStack, String key, String def) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.getOrCreateTag();

        if(!tagCompound.hasKeyOfType(key, 8))
            return def;

        return tagCompound.getString(key);
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack itemStack, String key, String value) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.getOrCreateTag();

        tagCompound.set(key, NBTTagString.a(value));

        nmsItem.setTag(tagCompound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public long getTag(org.bukkit.inventory.ItemStack itemStack, String key, long def) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.getOrCreateTag();

        if(!tagCompound.hasKeyOfType(key, 4))
            return def;

        return tagCompound.getLong(key);
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack itemStack, String key, long value) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.getOrCreateTag();

        tagCompound.set(key, NBTTagLong.a(value));

        nmsItem.setTag(tagCompound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public org.bukkit.inventory.ItemStack getPlayerSkull(org.bukkit.inventory.ItemStack itemStack, String texture) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        NBTTagCompound nbtTagCompound = nmsItem.getOrCreateTag();

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
    public ChunkLoaderNPC createNPC(Location location, UUID uuid) {
        return new ChunkLoaderNPC_v1_17_R1(((CraftServer) Bukkit.getServer()).getServer(), location, uuid);
    }

    @Override
    public ITileEntityChunkLoader createLoader(ChunkLoader chunkLoader) {
        Location loaderLoc = chunkLoader.getLocation();
        assert loaderLoc.getWorld() != null;
        WorldServer world = ((CraftWorld) loaderLoc.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(loaderLoc.getX(), loaderLoc.getY(), loaderLoc.getZ());

        TileEntityChunkLoader tileEntityChunkLoader = new TileEntityChunkLoader(chunkLoader, world, blockPosition);
        world.a(tileEntityChunkLoader.ticker);

        for(org.bukkit.Chunk bukkitChunk : chunkLoader.getLoadedChunks()) {
            Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
            chunk.l.values().stream().filter(tileEntity -> tileEntity instanceof TileEntityMobSpawner)
                    .forEach(tileEntity -> ((TileEntityMobSpawner) tileEntity).getSpawner().n = -1);

            world.setForceLoaded(chunk.getPos().b, chunk.getPos().c, true);
        }

        return tileEntityChunkLoader;
    }

    @Override
    public void removeLoader(ChunkLoader chunkLoader, boolean spawnParticle) {
        Location loaderLoc = chunkLoader.getLocation();
        assert loaderLoc.getWorld() != null;
        WorldServer world = ((CraftWorld) loaderLoc.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(loaderLoc.getX(), loaderLoc.getY(), loaderLoc.getZ());

        long tileEntityLong = ChunkCoordIntPair.pair(blockPosition.getX() >> 4, blockPosition.getZ() >> 4);
        TileEntityChunkLoader tileEntityChunkLoader = TileEntityChunkLoader.tileEntityChunkLoaderMap.remove(tileEntityLong);
        if(tileEntityChunkLoader != null) {
            tileEntityChunkLoader.holograms.forEach(EntityHolograms_v1_17_R1::removeHologram);
            tileEntityChunkLoader.removed = true;
        }

        if(spawnParticle)
            world.a(null, 2001, blockPosition, Block.getCombinedId(world.getType(blockPosition)));

        for(org.bukkit.Chunk bukkitChunk : chunkLoader.getLoadedChunks()) {
            Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
            chunk.l.values().stream().filter(tileEntity -> tileEntity instanceof TileEntityMobSpawner)
                    .forEach(tileEntity -> ((TileEntityMobSpawner) tileEntity).getSpawner().n = 16);

            world.setForceLoaded(chunk.getPos().b, chunk.getPos().c, false);
        }
    }

    @Override
    public void updateSpawner(Location location, boolean reset) {
        assert location.getWorld() != null;
        World world = ((CraftWorld) location.getWorld()).getHandle();

        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        IBlockData blockData = world.getType(blockPosition);
        TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner) world.getTileEntity(blockPosition);

        if(mobSpawner == null)
            return;

        mobSpawner.getSpawner().n = reset ? 16 : -1;
    }

    private static final class TileEntityChunkLoader extends TileEntity implements ITileEntityChunkLoader {

        private static final Map<Long, TileEntityChunkLoader> tileEntityChunkLoaderMap = new HashMap<>();

        private final List<EntityHolograms_v1_17_R1> holograms = new ArrayList<>();
        private final WChunkLoader chunkLoader;
        private final Block loaderBlock;
        private final TileEntityChunkLoaderTicker ticker;

        private short currentTick = 20;
        private short daysAmount, hoursAmount, minutesAmount, secondsAmount;
        private boolean removed = false;

        TileEntityChunkLoader(ChunkLoader chunkLoader, World world, BlockPosition blockPosition){
            super(TileEntityTypes.v, blockPosition, world.getType(blockPosition));

            this.chunkLoader = (WChunkLoader) chunkLoader;
            this.ticker = new TileEntityChunkLoaderTicker(this);

            setWorld(world);

            loaderBlock = world.getType(blockPosition).getBlock();

            if(!this.chunkLoader.isInfinite()) {
                long timeLeft = chunkLoader.getTimeLeft();

                daysAmount = (short) (timeLeft / 86400);
                timeLeft = timeLeft % 86400;

                hoursAmount = (short) (timeLeft / 3600);
                timeLeft = timeLeft % 3600;

                minutesAmount = (short) (timeLeft / 60);
                timeLeft = timeLeft % 60;

                secondsAmount = (short) timeLeft;
            }

            tileEntityChunkLoaderMap.put(ChunkCoordIntPair.pair(blockPosition.getX() >> 4, blockPosition.getZ() >> 4), this);

            List<String> hologramLines = this.chunkLoader.getHologramLines();

            double currentY = getPosition().getY() + 1;
            for(int i = hologramLines.size(); i > 0; i--){
                EntityHolograms_v1_17_R1 hologram = new EntityHolograms_v1_17_R1(world,
                        getPosition().getX() + 0.5, currentY, getPosition().getZ() + 0.5);
                updateName(hologram, hologramLines.get(i - 1));
                world.addEntity(hologram);
                currentY += 0.23;
                holograms.add(hologram);
            }
        }

        public void tick() {
            if(removed || ++currentTick <= 20)
                return;

            currentTick = 0;

            assert this.n != null;
            if(chunkLoader.isNotActive() || this.n.getType(getPosition()).getBlock() != loaderBlock){
                chunkLoader.remove();
                return;
            }

            if(chunkLoader.isInfinite())
                return;

            List<String> hologramLines = chunkLoader.getHologramLines();

            int hologramsAmount = holograms.size();
            for (int i = hologramsAmount; i > 0; i--) {
                EntityHolograms_v1_17_R1 hologram = holograms.get(hologramsAmount - i);
                updateName(hologram, hologramLines.get(i - 1));
            }

            chunkLoader.tick();

            if(!removed) {
                secondsAmount--;
                if (secondsAmount < 0) {
                    secondsAmount = 59;
                    minutesAmount--;
                    if (minutesAmount < 0) {
                        minutesAmount = 59;
                        hoursAmount--;
                        if (hoursAmount < 0) {
                            hoursAmount = 23;
                            daysAmount--;
                        }
                    }
                }
            }
        }

        @Override
        public Collection<Hologram> getHolograms() {
            return Collections.unmodifiableList(holograms);
        }

        @Override
        public boolean isRemoved() {
            return removed || super.isRemoved();
        }

        private void updateName(EntityHolograms_v1_17_R1 hologram, String line){
            assert chunkLoader.getWhoPlaced().getName() != null;
            hologram.setHologramName(line
                    .replace("{0}", chunkLoader.getWhoPlaced().getName())
                    .replace("{1}", daysAmount + "")
                    .replace("{2}", hoursAmount + "")
                    .replace("{3}", minutesAmount + "")
                    .replace("{4}", secondsAmount + "")
            );
        }

    }

    private record TileEntityChunkLoaderTicker(
            TileEntityChunkLoader tileEntityChunkLoader) implements TickingBlockEntity {

        @Override
        public void a() {
            tileEntityChunkLoader.tick();
        }

        @Override
        public boolean b() {
            return tileEntityChunkLoader.isRemoved();
        }

        @Override
        public BlockPosition c() {
            return tileEntityChunkLoader.getPosition();
        }

        @Override
        public String d() {
            return TileEntityTypes.a(tileEntityChunkLoader.getTileType()) + "";
        }
    }

}
