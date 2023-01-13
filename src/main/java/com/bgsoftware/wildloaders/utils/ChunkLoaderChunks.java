package com.bgsoftware.wildloaders.utils;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class ChunkLoaderChunks {

    private static final WildLoadersPlugin plugin = WildLoadersPlugin.getPlugin();

    private ChunkLoaderChunks() {

    }

    public static List<Chunk> calculateChunks(LoaderData loaderData, UUID whoPlaced, Location original) {
        List<Chunk> chunkList = new LinkedList<>();

        if (loaderData.isChunksSpread()) {
            calculateClaimChunksRecursive(original.getChunk(), whoPlaced, chunkList);
        }

        if (chunkList.isEmpty()) {
            int chunkX = original.getBlockX() >> 4, chunkZ = original.getBlockZ() >> 4;

            for (int x = -loaderData.getChunksRadius(); x <= loaderData.getChunksRadius(); x++)
                for (int z = -loaderData.getChunksRadius(); z <= loaderData.getChunksRadius(); z++)
                    chunkList.add(original.getWorld().getChunkAt(chunkX + x, chunkZ + z));
        }

        return chunkList;
    }

    private static void calculateClaimChunksRecursive(Chunk originalChunk, UUID whoPlaced, List<Chunk> chunkList) {
        if (!plugin.getProviders().hasChunkAccess(whoPlaced, originalChunk))
            return;

        chunkList.add(originalChunk);

        int chunkX = originalChunk.getX(), chunkZ = originalChunk.getZ();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x != 0 || z != 0) // We don't want to add the originalChunk again.
                    calculateClaimChunksRecursive(originalChunk.getWorld().getChunkAt(chunkX + x, chunkZ + z), whoPlaced, chunkList);
            }
        }

    }

}
