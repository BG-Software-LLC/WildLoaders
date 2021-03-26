package com.bgsoftware.wildloaders.nms;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.holograms.Hologram;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC;
import com.bgsoftware.wildloaders.loaders.ITileEntityChunkLoader;
import com.bgsoftware.wildloaders.loaders.WChunkLoader;
import net.minecraft.server.v1_11_R1.Block;
import net.minecraft.server.v1_11_R1.BlockPosition;
import net.minecraft.server.v1_11_R1.Chunk;
import net.minecraft.server.v1_11_R1.ITickable;
import net.minecraft.server.v1_11_R1.ItemStack;
import net.minecraft.server.v1_11_R1.NBTTagCompound;
import net.minecraft.server.v1_11_R1.NBTTagList;
import net.minecraft.server.v1_11_R1.NBTTagLong;
import net.minecraft.server.v1_11_R1.NBTTagString;
import net.minecraft.server.v1_11_R1.TileEntity;
import net.minecraft.server.v1_11_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_11_R1.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_11_R1.util.LongHash;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"ConstantConditions", "unused"})
public final class NMSAdapter_v1_11_R1 implements NMSAdapter {

    private static final WildLoadersPlugin plugin = WildLoadersPlugin.getPlugin();

    @Override
    public String getTag(org.bukkit.inventory.ItemStack itemStack, String key, String def) {
        ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tagCompound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        if(!tagCompound.hasKeyOfType(key, 8))
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

        if(!tagCompound.hasKeyOfType(key, 4))
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
    public ChunkLoaderNPC createNPC(Location location, UUID uuid) {
        return new ChunkLoaderNPC_v1_11_R1(location, uuid);
    }

    @Override
    public ITileEntityChunkLoader createLoader(ChunkLoader chunkLoader) {
        Location loaderLoc = chunkLoader.getLocation();
        World world = ((CraftWorld) loaderLoc.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(loaderLoc.getX(), loaderLoc.getY(), loaderLoc.getZ());

        TileEntityChunkLoader tileEntityChunkLoader = new TileEntityChunkLoader(chunkLoader, world, blockPosition);
        world.tileEntityListTick.add(tileEntityChunkLoader);

        for(org.bukkit.Chunk bukkitChunk : chunkLoader.getLoadedChunks()) {
            Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
            chunk.tileEntities.values().stream().filter(tileEntity -> tileEntity instanceof TileEntityMobSpawner).forEach(tileEntity -> {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                tileEntity.save(nbtTagCompound);
                nbtTagCompound.setShort("RequiredPlayerRange", (short) -1);
                tileEntity.a(nbtTagCompound);
            });
        }

        return tileEntityChunkLoader;
    }

    @Override
    public void removeLoader(ChunkLoader chunkLoader, boolean spawnParticle) {
        Location loaderLoc = chunkLoader.getLocation();
        World world = ((CraftWorld) loaderLoc.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(loaderLoc.getX(), loaderLoc.getY(), loaderLoc.getZ());

        long tileEntityLong = LongHash.toLong(blockPosition.getX() >> 4, blockPosition.getZ() >> 4);
        TileEntityChunkLoader tileEntityChunkLoader = TileEntityChunkLoader.tileEntityChunkLoaderMap.remove(tileEntityLong);
        if(tileEntityChunkLoader != null) {
            tileEntityChunkLoader.holograms.forEach(EntityHolograms_v1_11_R1::removeHologram);
            tileEntityChunkLoader.removed = true;
            world.tileEntityListTick.remove(tileEntityChunkLoader);
        }

        if(spawnParticle)
            world.a(null, 2001, blockPosition, Block.getCombinedId(world.getType(blockPosition)));

        for(org.bukkit.Chunk bukkitChunk : chunkLoader.getLoadedChunks()) {
            Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();
            chunk.tileEntities.values().stream().filter(tileEntity -> tileEntity instanceof TileEntityMobSpawner).forEach(tileEntity -> {
                NBTTagCompound nbtTagCompound = new NBTTagCompound();
                tileEntity.save(nbtTagCompound);
                nbtTagCompound.setShort("RequiredPlayerRange", (short) 16);
                tileEntity.a(nbtTagCompound);
            });
        }
    }

    @Override
    public void updateSpawner(Location location, boolean reset) {
        World world = ((CraftWorld) location.getWorld()).getHandle();

        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        TileEntityMobSpawner mobSpawner = (TileEntityMobSpawner) world.getTileEntity(blockPosition);

        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        mobSpawner.save(nbtTagCompound);
        nbtTagCompound.setShort("RequiredPlayerRange", (short) (reset ? 16 : -1));
        mobSpawner.a(nbtTagCompound);
    }

    private static final class TileEntityChunkLoader extends TileEntity implements ITickable, ITileEntityChunkLoader {

        private static final Map<Long, TileEntityChunkLoader> tileEntityChunkLoaderMap = new HashMap<>();

        private final List<EntityHolograms_v1_11_R1> holograms = new ArrayList<>();
        private final WChunkLoader chunkLoader;
        private final Block loaderBlock;

        private short currentTick = 20;
        private short daysAmount, hoursAmount, minutesAmount, secondsAmount;
        private boolean removed = false;

        TileEntityChunkLoader(ChunkLoader chunkLoader, World world, BlockPosition blockPosition){
            this.chunkLoader = (WChunkLoader) chunkLoader;

            setPosition(blockPosition);
            a(world);

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

            tileEntityChunkLoaderMap.put(LongHash.toLong(blockPosition.getX() >> 4, blockPosition.getZ() >> 4), this);

            List<String> hologramLines = this.chunkLoader.getHologramLines();

            double currentY = position.getY() + 1;
            for(int i = hologramLines.size(); i > 0; i--){
                EntityHolograms_v1_11_R1 hologram = new EntityHolograms_v1_11_R1(world, position.getX() + 0.5, currentY, position.getZ() + 0.5);
                updateName(hologram, hologramLines.get(i - 1));
                world.addEntity(hologram);
                currentY += 0.23;
                holograms.add(hologram);
            }
        }

        @Override
        public void F_() {
            if(removed || ++currentTick <= 20)
                return;

            currentTick = 0;

            if(chunkLoader.isNotActive() || world.getType(position).getBlock() != loaderBlock){
                chunkLoader.remove();
                return;
            }

            if(chunkLoader.isInfinite())
                return;

            List<String> hologramLines = chunkLoader.getHologramLines();

            int hologramsAmount = holograms.size();
            for (int i = hologramsAmount; i > 0; i--) {
                EntityHolograms_v1_11_R1 hologram = holograms.get(hologramsAmount - i);
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

        private void updateName(EntityHolograms_v1_11_R1 hologram, String line){
            hologram.setHologramName(line
                    .replace("{0}", chunkLoader.getWhoPlaced().getName())
                    .replace("{1}", daysAmount + "")
                    .replace("{2}", hoursAmount + "")
                    .replace("{3}", minutesAmount + "")
                    .replace("{4}", secondsAmount + "")
            );
        }

    }

}
