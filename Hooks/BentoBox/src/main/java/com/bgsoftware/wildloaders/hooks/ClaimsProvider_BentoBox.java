package com.bgsoftware.wildloaders.hooks;

import world.bentobox.BentoBox;

import com.bgsoftware.wildloaders.api.hooks.ClaimsProvider;
import org.bukkit.Chunk;

import java.util.UUID;

public final class ClaimsProvider_BentoBox implements ClaimsProvider {

    private static final IslandPrivilege BUILD = IslandPrivilege.getByName("BUILD");

    @Override
    public boolean hasClaimAccess(UUID player, Chunk chunk) {
        Island island = BentoBox.getGrid().getIslandAt(chunk.getX(), chunk.getZ());
        User bentoPlayer = BentoBox.getPlayersManager().getUser(player);
        return island != null && island.isAllowed(bentoPlayer, Flags.PLACE_BLOCKS) && island.inIslandSpace(chunk.getX(), chunk.getZ());
    }

}
