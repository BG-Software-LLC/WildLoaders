package com.bgsoftware.wildloaders.utils;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.scheduler.Scheduler;
import com.bgsoftware.wildloaders.utils.chunks.ChunkPosition;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class ChunkLoaderChunks {

    private static final int MAX_DEPTH = 200;

    private static final WildLoadersPlugin plugin = WildLoadersPlugin.getPlugin();

    private ChunkLoaderChunks() {

    }

    public static List<ChunkPosition> calculateChunks(LoaderData loaderData, UUID whoPlaced, Location original) {
        Set<ChunkPosition> chunkList = new LinkedHashSet<>();

        int chunkX = original.getBlockX() >> 4;
        int chunkZ = original.getBlockZ() >> 4;
        World world = original.getWorld();

        if (loaderData.isChunksSpread()) {
            ChunkPosition chunkPosition = ChunkPosition.of(world, chunkX, chunkZ);
            calculateClaimChunksRecursive(world, chunkPosition, whoPlaced, chunkList);
        }

        if (chunkList.isEmpty()) {
            for (int x = -loaderData.getChunksRadius(); x <= loaderData.getChunksRadius(); x++) {
                for (int z = -loaderData.getChunksRadius(); z <= loaderData.getChunksRadius(); z++) {
                    chunkList.add(ChunkPosition.of(world, chunkX + x, chunkZ + z));
                }
            }
        }

        return chunkList.isEmpty() ? Collections.emptyList() : new LinkedList<>(chunkList);
    }

    private static void calculateClaimChunksRecursive(World world, ChunkPosition chunkPosition, UUID whoPlaced, Set<ChunkPosition> chunkList) {
        calculateClaimChunksRecursiveInternal(world, chunkPosition, whoPlaced, chunkList, 0);
    }

    private static void calculateClaimChunksRecursiveInternal(World world, ChunkPosition originalChunk, UUID whoPlaced, Set<ChunkPosition> chunkList, int depth) {
        if (depth > MAX_DEPTH) {
            WildLoadersPlugin.log("Chunk list: " + chunkList);
            throw new IllegalStateException("Called calculateClaimChunksRecursive with depth " + depth);
        }

        if (chunkList.contains(originalChunk) ||
                !plugin.getProviders().hasChunkAccess(whoPlaced, world, originalChunk.getX(), originalChunk.getZ()))
            return;

        chunkList.add(originalChunk);

        int chunkX = originalChunk.getX();
        int chunkZ = originalChunk.getZ();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                // We don't want to add the originalChunk again.
                if (x != 0 || z != 0) {
                    ChunkPosition chunkPosition = ChunkPosition.of(originalChunk.getWorld(), chunkX + x, chunkZ + z);
                    calculateClaimChunksRecursiveInternal(world, chunkPosition, whoPlaced, chunkList, depth + 1);
                }
            }
        }

    }

    public static void loadChunksAsync(ChunkLoader chunkLoader, List<ChunkPosition> chunksToLoad,
                                       @Nullable Consumer<Chunk> consumer, @Nullable Runnable onFinish) {
        loadChunksAsync(chunkLoader.getLocation(), chunksToLoad, consumer, onFinish);
    }

    public static void loadChunksAsync(Location location, List<ChunkPosition> chunksToLoad,
                                       @Nullable Consumer<Chunk> consumer, @Nullable Runnable onFinish) {
        CountDownLatch latch = new CountDownLatch(chunksToLoad.size());
        chunksToLoad.forEach(chunkPosition -> {
            World world = chunkPosition.getBukkitWorld();
            if (world == null) {
                world = plugin.getProviders().loadWorld(chunkPosition.getWorld());
                if (world == null) {
                    latch.countDown();
                    return;
                }
            }
            plugin.getProviders().loadChunk(world, chunkPosition.getX(), chunkPosition.getZ(), chunk -> {
                if (consumer != null)
                    consumer.accept(chunk);
                latch.countDown();
            });
        });

        Scheduler.runTaskAsync(() -> {
            try {
                latch.await();
            } catch (Throwable error) {
                error.printStackTrace();
            }
            if (onFinish != null)
                Scheduler.runTask(location, onFinish);
        });
    }
}
