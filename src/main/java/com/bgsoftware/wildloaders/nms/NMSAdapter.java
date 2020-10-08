package com.bgsoftware.wildloaders.nms;

import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC;
import com.bgsoftware.wildloaders.loaders.ITileEntityChunkLoader;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface NMSAdapter {

    String getTag(ItemStack itemStack, String key, String def);

    ItemStack setTag(ItemStack itemStack, String key, String value);

    long getTag(ItemStack itemStack, String key, long def);

    ItemStack setTag(ItemStack itemStack, String key, long value);

    ItemStack getPlayerSkull(ItemStack itemStack, String texture);

    ChunkLoaderNPC createNPC(Location location, UUID uuid);

    ITileEntityChunkLoader createLoader(ChunkLoader chunkLoader);

    void removeLoader(ChunkLoader chunkLoader, boolean spawnParticle);

    void updateSpawner(Location location, boolean reset);

}
