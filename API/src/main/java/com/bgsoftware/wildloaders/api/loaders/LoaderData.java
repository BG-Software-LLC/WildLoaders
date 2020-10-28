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
     * Whether or not the chunk loader is infinite.
     */
    boolean isInfinite();

    /**
     * Get the drop item of the chunk loader, with default time.
     */
    ItemStack getLoaderItem();

    /**
     * Get the drop item of the chunk loader, with a specific time left.
     */
    ItemStack getLoaderItem(long timeLeft);

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

    /**
     * Set whether or not the chunks-spread mode is enabled for this chunk loader.
     * When this mode is enabled, all the chunks inside a claim that are connected to each other will be claimed.
     * @param chunksSpread Set the status of the chunks spread mode.
     */
    void setChunksSpread(boolean chunksSpread);

    /**
     * Get whether or not the chunks-spread mode is enabled for this chunk loader.
     * When this mode is enabled, all the chunks inside a claim that are connected to each other will be claimed.
     */
    boolean isChunksSpread();

}
