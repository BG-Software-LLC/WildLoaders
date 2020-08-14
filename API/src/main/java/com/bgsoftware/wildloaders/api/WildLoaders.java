package com.bgsoftware.wildloaders.api;

import com.bgsoftware.wildloaders.api.managers.LoadersManager;
import com.bgsoftware.wildloaders.api.managers.NPCManager;

public interface WildLoaders {

    /**
     * Get the chunk loaders manager.
     */
    LoadersManager getLoaders();

    /**
     * Get the npcs manager.
     */
    NPCManager getNPCs();

}
