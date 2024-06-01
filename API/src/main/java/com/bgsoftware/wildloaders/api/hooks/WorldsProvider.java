package com.bgsoftware.wildloaders.api.hooks;

import org.bukkit.World;

import javax.annotation.Nullable;

public interface WorldsProvider {

    /**
     * Load a world.
     *
     * @param worldName The name of the world
     * @return The loaded world, or null if couldn't load the world.
     */
    @Nullable
    World loadWorld(String worldName);

}
