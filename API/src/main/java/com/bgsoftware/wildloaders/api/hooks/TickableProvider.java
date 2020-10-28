package com.bgsoftware.wildloaders.api.hooks;

import org.bukkit.Chunk;

public interface TickableProvider {

    /**
     * Simulate a tick on a list of provided chunks.
     * @param chunks The chunks to tick.
     */
    void tick(Chunk[] chunks);

}
