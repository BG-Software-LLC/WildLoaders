package com.bgsoftware.wildloaders.utils;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ChunkLoaderChunks {

    private static final int MAX_DEPTH = 200;

    private static final WildLoadersPlugin plugin = WildLoadersPlugin.getPlugin();

    private ChunkLoaderChunks() {

    }

    public static List<Chunk> calculateChunks(LoaderData loaderData, UUID whoPlaced, Location original) {
        Set<Chunk> chunkList = new LinkedHashSet<>();

        if (loaderData.isChunksSpread()) {
            calculateClaimChunksRecursive(original.getChunk(), whoPlaced, chunkList);
        }

        if (chunkList.isEmpty()) {
            int chunkX = original.getBlockX() >> 4, chunkZ = original.getBlockZ() >> 4;

            for (int x = -loaderData.getChunksRadius(); x <= loaderData.getChunksRadius(); x++)
                for (int z = -loaderData.getChunksRadius(); z <= loaderData.getChunksRadius(); z++)
                    chunkList.add(original.getWorld().getChunkAt(chunkX + x, chunkZ + z));
        }

        return chunkList.isEmpty() ? Collections.emptyList() : new LinkedList<>(chunkList);
    }

    private static void calculateClaimChunksRecursive(Chunk originalChunk, UUID whoPlaced, Set<Chunk> chunkList) {
        calculateClaimChunksRecursive(originalChunk, whoPlaced, chunkList, 0);
    }

    private static void calculateClaimChunksRecursive(Chunk originalChunk, UUID whoPlaced, Set<Chunk> chunkList, int depth) {
        if (depth > MAX_DEPTH) {
            WildLoadersPlugin.log("Chunk list: " + chunkList);
            throw new IllegalStateException("Called calculateClaimChunksRecursive with depth " + depth);
        }

        if (chunkList.contains(originalChunk) || !plugin.getProviders().hasChunkAccess(whoPlaced, originalChunk))
            return;

        chunkList.add(originalChunk);

        int chunkX = originalChunk.getX();
        int chunkZ = originalChunk.getZ();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x != 0 || z != 0) // We don't want to add the originalChunk again.
                    calculateClaimChunksRecursive(originalChunk.getWorld().getChunkAt(chunkX + x, chunkZ + z),
                            whoPlaced, chunkList, depth + 1);
            }
        }

    }

}
