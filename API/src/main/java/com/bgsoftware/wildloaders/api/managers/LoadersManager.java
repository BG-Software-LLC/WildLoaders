package com.bgsoftware.wildloaders.api.managers;

import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public interface LoadersManager {

    /**
     * Get an active chunk loader from a chunk.
     * @param chunk The chunk to check.
     */
    Optional<ChunkLoader> getChunkLoader(Chunk chunk);

    /**
     * Get a chunk loader by it's location.
     * @param location The location of the chunk loader.
     */
    Optional<ChunkLoader> getChunkLoader(Location location);

    /**
     * Get all the chunk loaders on the server.
     */
    List<ChunkLoader> getChunkLoaders();

    /**
     * Get chunk-loader data by it's name.
     * @param name The name of the data.
     */
    Optional<LoaderData> getLoaderData(String name);

    /**
     * Get all the available chunk-loaders data.
     */
    List<LoaderData> getLoaderDatas();

    /**
     * Create a new chunk loader at a specific location.
     * @param loaderData The data of the chunk loader.
     * @param whoPlaced The player who placed the chunk loader.
     * @param location The location of the chunk loader.
     * @param timeLeft The amount of time left for the chunk loader to run.
     * @return The new chunk loader object.
     */
    ChunkLoader addChunkLoader(LoaderData loaderData, Player whoPlaced, Location location, long timeLeft);

    /**
     * Remove a chunk loader from the database.
     * It's recommended to use ChunkLoader#remove instead!
     */
    void removeChunkLoader(ChunkLoader chunkLoader);

    /**
     * Create a new chunk-loader data.
     * @param name The name of the data.
     * @param timeLeft The default amount of time to run.
     * @param itemStack The item stack to drop upon break.
     */
    LoaderData createLoaderData(String name, long timeLeft, ItemStack itemStack);

    /**
     * Remove all the chunk loaders data from cache.
     */
    void removeLoadersData();

    /**
     * Remove all chunk loaders from cache.
     */
    void removeChunkLoaders();

}
