package com.bgsoftware.wildloaders.api.managers;

import com.bgsoftware.wildloaders.api.hooks.ClaimsProvider;

public interface ProvidersManager {

    /**
     * Add a claims provider to the plugin.
     * @param claimsProvider The claims provider to add.
     */
    void addClaimsProvider(ClaimsProvider claimsProvider);

}
