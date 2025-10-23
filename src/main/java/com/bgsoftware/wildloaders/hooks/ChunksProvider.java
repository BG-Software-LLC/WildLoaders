package com.bgsoftware.wildloaders.hooks;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.function.Consumer;

public interface ChunksProvider {

    void loadChunk(World world, int chunkX, int chunkZ, Consumer<Chunk> consumer);

}
