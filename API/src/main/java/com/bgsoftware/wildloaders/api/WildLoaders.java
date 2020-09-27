package com.bgsoftware.wildloaders.api;

import com.bgsoftware.wildloaders.api.managers.LoadersManager;
import com.bgsoftware.wildloaders.api.managers.NPCManager;
import com.bgsoftware.wildloaders.api.managers.ProvidersManager;

public interface WildLoaders {

    /**
     * Get the chunk loaders manager.
     */
    LoadersManager getLoaders();

    /**
     * Get the npcs manager.
     */
    NPCManager getNPCs();

    /**
     * Get the providers manager.
     */
    ProvidersManager getProviders();

}
