package com.bgsoftware.wildloaders.api;

import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.Optional;

public final class WildLoadersAPI {

    private static WildLoaders instance;

    /**
     * Get an active chunk loader from a chunk.
     * @param chunk The chunk to check.
     */
    public static Optional<ChunkLoader> getChunkLoader(Chunk chunk){
        return instance.getLoaders().getChunkLoader(chunk);
    }

    /**
     * Get a chunk loader by it's location.
     * @param location The location of the chunk loader.
     */
    public static Optional<ChunkLoader> getChunkLoader(Location location){
        return instance.getLoaders().getChunkLoader(location);
    }

    /**
     * Remove a chunk loader from the database.
     * It's recommended to use ChunkLoader#remove instead!
     */
    public static void removeChunkLoader(ChunkLoader chunkLoader){
        instance.getLoaders().removeChunkLoader(chunkLoader);
    }

    /**
     * Get the wildloaders object.
     */
    public static WildLoaders getWildLoaders() {
        return instance;
    }

}
