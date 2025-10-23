package com.bgsoftware.wildloaders.api.hooks;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.UUID;

public interface ClaimsProvider {

    @Deprecated
    default boolean hasClaimAccess(UUID player, Chunk chunk) {
        return hasClaimAccess(player, chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    default boolean hasClaimAccess(UUID player, World world, int chunkX, int chunkZ) {
        throw new UnsupportedOperationException("Implement this");
    }

}
