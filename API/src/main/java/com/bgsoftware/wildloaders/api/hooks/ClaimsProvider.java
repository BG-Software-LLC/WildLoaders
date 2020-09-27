package com.bgsoftware.wildloaders.api.hooks;

import org.bukkit.Chunk;

import java.util.UUID;

public interface ClaimsProvider {

    boolean hasClaimAccess(UUID player, Chunk chunk);

}
