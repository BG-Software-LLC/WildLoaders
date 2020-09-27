package com.bgsoftware.wildloaders.api.loaders;

import org.bukkit.inventory.ItemStack;

public interface LoaderData {

    /**
     * Get the name of the data.
     */
    String getName();

    /**
     * Get the default amount of time the chunk loader will run for.
     */
    long getTimeLeft();

    /**
     * Get the drop item of the chunk loader, with default time.
     */
    ItemStack getLoaderItem();

    /**
     * Set the radius of chunks that the chunk loader will load.
     * If the radius is 0, it means only one chunk is loaded. A radius of 1, will load 3x3 chunks, etc.
     * Please note: In some versions, when loading one chunk, the nearby chunks are also being loaded!
     * @param chunksRadius The chunk radius to set.
     */
    void setChunksRadius(int chunksRadius);

    /**
     * Get the radius of chunks that the chunk loader will load.
     * If the radius is 0, it means only one chunk is loaded. A radius of 1, will load 3x3 chunks, etc.
     * Please note: In some versions, when loading one chunk, the nearby chunks are also being loaded!
     */
    int getChunksRadius();

}
