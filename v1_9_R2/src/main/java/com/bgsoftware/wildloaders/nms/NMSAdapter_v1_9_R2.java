package com.bgsoftware.wildloaders.nms;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC;
import com.bgsoftware.wildloaders.loaders.WChunkLoader;
import net.minecraft.server.v1_9_R2.Block;
import net.minecraft.server.v1_9_R2.BlockPosition;
import net.minecraft.server.v1_9_R2.Chunk;
import net.minecraft.server.v1_9_R2.ITickable;
import net.minecraft.server.v1_9_R2.ItemStack;
import net.minecraft.server.v1_9_R2.NBTTagCompound;
import net.minecraft.server.v1_9_R2.NBTTagList;
import net.minecraft.server.v1_9_R2.NBTTagLong;
import net.minecraft.server.v1_9_R2.NBTTagString;
import net.minecraft.server.v1_9_R2.TileEntity;
import net.minecraft.server.v1_9_R2.TileEntityMobSpawner;
import net.minecraft.server.v1_9_R2.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_9_R2.util.LongHash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings({"ConstantConditions", "unused"})
public final class NMSAdapter_v1_9_R2 implements NMSAdapter {

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
        return new ChunkLoaderNPC_v1_9_R2(location, uuid);
    }

    @Override
    public void createLoader(ChunkLoader chunkLoader) {
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
    }

    @Override
    public void removeLoader(ChunkLoader chunkLoader, boolean spawnParticle) {
        Location loaderLoc = chunkLoader.getLocation();
        World world = ((CraftWorld) loaderLoc.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(loaderLoc.getX(), loaderLoc.getY(), loaderLoc.getZ());

        long tileEntityLong = LongHash.toLong(blockPosition.getX() >> 4, blockPosition.getZ() >> 4);
        TileEntityChunkLoader tileEntityChunkLoader = TileEntityChunkLoader.tileEntityChunkLoaderMap.remove(tileEntityLong);
        if(tileEntityChunkLoader != null) {
            tileEntityChunkLoader.holograms.forEach(EntityHolograms_v1_9_R2::removeHologram);
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

    private static final class TileEntityChunkLoader extends TileEntity implements ITickable {

        private static final Map<Long, TileEntityChunkLoader> tileEntityChunkLoaderMap = new HashMap<>();

        private final List<EntityHolograms_v1_9_R2> holograms = new ArrayList<>();
        private final ChunkLoader chunkLoader;

        private short currentTick = 20;
        private short daysAmount, hoursAmount, minutesAmount, secondsAmount;
        private boolean removed = false;

        TileEntityChunkLoader(ChunkLoader chunkLoader, World world, BlockPosition blockPosition){
            this.chunkLoader = chunkLoader;

            a(blockPosition);
            a(world);

            long timeLeft = chunkLoader.getTimeLeft();

            daysAmount = (short) (timeLeft / 86400);
            timeLeft = timeLeft % 86400;

            hoursAmount = (short) (timeLeft / 3600);
            timeLeft = timeLeft % 3600;

            minutesAmount = (short) (timeLeft / 60);
            timeLeft = timeLeft % 60;

            secondsAmount = (short) timeLeft;

            tileEntityChunkLoaderMap.put(LongHash.toLong(blockPosition.getX() >> 4, blockPosition.getZ() >> 4), this);

            double currentY = position.getY() + 1;
            for(int i = plugin.getSettings().hologramLines.size(); i > 0; i--){
                EntityHolograms_v1_9_R2 hologram = new EntityHolograms_v1_9_R2(world, position.getX() + 0.5, currentY, position.getZ() + 0.5);
                updateName(hologram, plugin.getSettings().hologramLines.get(i - 1));
                world.addEntity(hologram);
                currentY += 0.23;
                holograms.add(hologram);
            }
        }

        @Override
        public void c() {
            if(removed || ++currentTick <= 20)
                return;

            currentTick = 0;

            if(((WChunkLoader) chunkLoader).isNotActive()){
                chunkLoader.remove();
                return;
            }

            int hologramsAmount = holograms.size();
            for (int i = hologramsAmount; i > 0; i--) {
                EntityHolograms_v1_9_R2 hologram = holograms.get(hologramsAmount - i);
                updateName(hologram, plugin.getSettings().hologramLines.get(i - 1));
            }

            ((WChunkLoader) chunkLoader).tick();

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

        private void updateName(EntityHolograms_v1_9_R2 hologram, String line){
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
