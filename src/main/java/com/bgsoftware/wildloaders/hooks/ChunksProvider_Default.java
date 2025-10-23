package com.bgsoftware.wildloaders.hooks;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.function.Consumer;

public class ChunksProvider_Default implements ChunksProvider {

    @Override
    public void loadChunk(World world, int chunkX, int chunkZ, Consumer<Chunk> consumer) {
        consumer.accept(world.getChunkAt(chunkX, chunkZ));
    }

}
