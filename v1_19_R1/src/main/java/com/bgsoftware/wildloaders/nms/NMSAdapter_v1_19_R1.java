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
import org.bukkit.craftbukkit.v1_19_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.bgsoftware.wildloaders.nms.NMSMappings_v1_19_R1.*;

@SuppressWarnings("unused")
public final class NMSAdapter_v1_19_R1 implements NMSAdapter {

    private static final WildLoadersPlugin plugin = WildLoadersPlugin.getPlugin();

    private static final ReflectMethod<TickingBlockEntity> CREATE_TICKING_BLOCK = new ReflectMethod<>(
            Chunk.class, "a", TileEntity.class, BlockEntityTicker.class);

    @Override
    public String getTag(org.bukkit.inventory.ItemStack itemStack, String key, String def) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = getOrCreateTag(nmsItem);

        if(!contains(tagCompound, key, 8))
            return def;

        return getString(tagCompound, key);
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack itemStack, String key, String value) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = getOrCreateTag(nmsItem);

        put(tagCompound, key, NBTTagString.a(value));

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public long getTag(org.bukkit.inventory.ItemStack itemStack, String key, long def) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = getOrCreateTag(nmsItem);

        if(!contains(tagCompound, key, 4))
            return def;

        return getLong(tagCompound, key);
    }

    @Override
    public org.bukkit.inventory.ItemStack setTag(org.bukkit.inventory.ItemStack itemStack, String key, long value) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = getOrCreateTag(nmsItem);

        put(tagCompound, key, NBTTagLong.a(value));

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public org.bukkit.inventory.ItemStack getPlayerSkull(org.bukkit.inventory.ItemStack itemStack, String texture) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);

        NBTTagCompound nbtTagCompound = getOrCreateTag(nmsItem);

        NBTTagCompound skullOwner = NMSMappings_v1_19_R1.contains(nbtTagCompound, "SkullOwner") ?
                getCompound(nbtTagCompound, "SkullOwner") : new NBTTagCompound();

        NBTTagCompound properties = new NBTTagCompound();

        NBTTagList textures = new NBTTagList();
        NBTTagCompound signature = new NBTTagCompound();
        putString(signature, "Value", texture);
        textures.add(signature);

        put(properties, "textures", textures);

        put(skullOwner, "Properties", properties);
        putString(skullOwner,"Id", UUID.randomUUID().toString());

        put(nbtTagCompound, "SkullOwner", skullOwner);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public ChunkLoaderNPC createNPC(Location location, UUID uuid) {
        return new ChunkLoaderNPC_v1_19_R1(((CraftServer) Bukkit.getServer()).getServer(), location, uuid);
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
            getBlockEntities(chunk).values().stream().filter(tileEntity -> tileEntity instanceof TileEntityMobSpawner)
                    .forEach(tileEntity -> getSpawner((TileEntityMobSpawner) tileEntity).m = -1);

            ChunkCoordIntPair chunkCoords = getPos(chunk);

            setChunkForced(world, chunkCoords.c, chunkCoords.d, true);
        }

        return tileEntityChunkLoader;
    }

    @Override
    public void removeLoader(ChunkLoader chunkLoader, boolean spawnParticle) {
        Location loaderLoc = chunkLoader.getLocation();
        assert loaderLoc.getWorld() != null;
        WorldServer world = ((CraftWorld) loaderLoc.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(loaderLoc.getX(), loaderLoc.getY(), loaderLoc.getZ());

        long tileEntityLong = ChunkCoordIntPair.c(getX(blockPosition) >> 4, getZ(blockPosition) >> 4);
        TileEntityChunkLoader tileEntityChunkLoader = TileEntityChunkLoader.tileEntityChunkLoaderMap.remove(tileEntityLong);
        if(tileEntityChunkLoader != null) {
            tileEntityChunkLoader.holograms.forEach(EntityHolograms_v1_19_R1::removeHologram);
            tileEntityChunkLoader.removed = true;
        }

        if(spawnParticle)
            world.a(null, 2001, blockPosition, getId(getBlockState(world, blockPosition)));

        for(org.bukkit.Chunk bukkitChunk : chunkLoader.getLoadedChunks()) {
            Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
            getBlockEntities(chunk).values().stream().filter(tileEntity -> tileEntity instanceof TileEntityMobSpawner)
                    .forEach(tileEntity -> getSpawner((TileEntityMobSpawner) tileEntity).m = 16);

            ChunkCoordIntPair chunkCoords = getPos(chunk);

            setChunkForced(world, chunkCoords.c, chunkCoords.d, false);
        }
    }

    @Override
    public void updateSpawner(Location location, boolean reset) {
        assert location.getWorld() != null;
        World world = ((CraftWorld) location.getWorld()).getHandle();

        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        IBlockData blockData = getBlockState(world, blockPosition);
        TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner) getBlockEntity(world, blockPosition);

        if(mobSpawner == null)
            return;

        getSpawner(mobSpawner).m = reset ? 16 : -1;
    }

    private static final class TileEntityChunkLoader extends TileEntity implements ITileEntityChunkLoader {

        private static final Map<Long, TileEntityChunkLoader> tileEntityChunkLoaderMap = new HashMap<>();

        private final List<EntityHolograms_v1_19_R1> holograms = new ArrayList<>();
        private final WChunkLoader chunkLoader;
        private final Block loaderBlock;
        private final TileEntityChunkLoaderTicker ticker;

        private short currentTick = 20;
        private short daysAmount, hoursAmount, minutesAmount, secondsAmount;
        private boolean removed = false;

        TileEntityChunkLoader(ChunkLoader chunkLoader, World world, BlockPosition blockPosition){
            super(TileEntityTypes.v, blockPosition, getBlockState(world, blockPosition));

            this.chunkLoader = (WChunkLoader) chunkLoader;
            this.ticker = new TileEntityChunkLoaderTicker(this);

            a(world);

            loaderBlock = getBlock(getBlockState(world, blockPosition));

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

            tileEntityChunkLoaderMap.put(ChunkCoordIntPair.c(getX(blockPosition) >> 4, getZ(blockPosition) >> 4), this);

            List<String> hologramLines = this.chunkLoader.getHologramLines();

            double currentY = getY(getBlockPos(this)) + 1;
            for(int i = hologramLines.size(); i > 0; i--){
                EntityHolograms_v1_19_R1 hologram = new EntityHolograms_v1_19_R1(world,
                        getX(getBlockPos(this)) + 0.5, currentY, getZ(getBlockPos(this)) + 0.5);
                updateName(hologram, hologramLines.get(i - 1));
                addFreshEntity(world, hologram);
                currentY += 0.23;
                holograms.add(hologram);
            }
        }

        public void tick() {
            if(removed || ++currentTick <= 20)
                return;

            currentTick = 0;

            assert this.n != null;
            if(chunkLoader.isNotActive() || getBlock(getBlockState(this.n, getBlockPos(this))) != loaderBlock){
                chunkLoader.remove();
                return;
            }

            if(chunkLoader.isInfinite())
                return;

            List<String> hologramLines = chunkLoader.getHologramLines();

            int hologramsAmount = holograms.size();
            for (int i = hologramsAmount; i > 0; i--) {
                EntityHolograms_v1_19_R1 hologram = holograms.get(hologramsAmount - i);
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
        public boolean r() {
            return removed || super.r();
        }

        private void updateName(EntityHolograms_v1_19_R1 hologram, String line){
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
            return tileEntityChunkLoader.r();
        }

        @Override
        public BlockPosition c() {
            return getBlockPos(tileEntityChunkLoader);
        }

        @Override
        public String d() {
            return TileEntityTypes.a(getType(tileEntityChunkLoader)) + "";
        }
    }

}
