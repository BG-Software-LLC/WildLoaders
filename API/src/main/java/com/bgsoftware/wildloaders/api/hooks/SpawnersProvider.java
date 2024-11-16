package com.bgsoftware.wildloaders.api.hooks;

import org.bukkit.Location;

public interface SpawnersProvider {

    /**
     * Sets the required-player range for the spawner in {@param spawnerLocation}.
     *
     * @param spawnerLocation The location of the spawner to set the range to.
     * @param requiredRange   The range to set.
     *                        Negative value means unlimited range.
     */
    void setSpawnerRequiredRange(Location spawnerLocation, int requiredRange);

}
