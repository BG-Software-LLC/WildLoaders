package com.bgsoftware.wildloaders.api.managers;

import com.bgsoftware.wildloaders.api.hooks.ClaimsProvider;
import com.bgsoftware.wildloaders.api.hooks.SpawnersProvider;
import com.bgsoftware.wildloaders.api.hooks.TickableProvider;
import com.bgsoftware.wildloaders.api.hooks.WorldsProvider;

public interface ProvidersManager {

    /**
     * Add a claims provider to the plugin.
     * @param claimsProvider The claims provider to add.
     */
    void addClaimsProvider(ClaimsProvider claimsProvider);

    /**
     * Add a spawners provider to the plugin.
     * @param spawnersProvider The spawners provider to add.
     */
    void addSpawnersProvider(SpawnersProvider spawnersProvider);

    /**
     * Add a tickable provider to the plugin.
     * @param tickableProvider The tickable provider to add.
     */
    void addTickableProvider(TickableProvider tickableProvider);

    /**
     * Add a worlds provider to the plugin.
     *
     * @param worldsProvider The worlds provider to add.
     */
    void addWorldsProvider(WorldsProvider worldsProvider);

}
