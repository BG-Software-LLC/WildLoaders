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

}
