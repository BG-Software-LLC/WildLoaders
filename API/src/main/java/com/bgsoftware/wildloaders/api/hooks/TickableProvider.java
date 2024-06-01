package com.bgsoftware.wildloaders.api.hooks;

import org.bukkit.Chunk;

import java.util.Collection;

public interface TickableProvider {

    /**
     * Simulate a tick on a list of provided chunks.
     *
     * @param chunks The chunks to tick.
     * @deprecated See {@link #tick(Collection)}
     */
    @Deprecated
    default void tick(Chunk[] chunks) {
        throw new UnsupportedOperationException("TickableProvider#tick is not supported anymore");
    }

    /**
     * Simulate a tick on a list of provided chunks.
     *
     * @param chunks The chunks to tick.
     */
    default void tick(Collection<Chunk> chunks) {
        tick(chunks.toArray(new Chunk[0]));
    }

}
