package com.bgsoftware.wildloaders.hooks;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.wildloaders.api.hooks.ClaimsProvider;
import org.bukkit.Chunk;

import java.util.UUID;

public final class ClaimsProvider_SuperiorSkyblock implements ClaimsProvider {

    private static final IslandPrivilege BUILD = IslandPrivilege.getByName("BUILD");

    @Override
    public boolean hasClaimAccess(UUID player, Chunk chunk) {
        Island island = SuperiorSkyblockAPI.getGrid().getIslandAt(chunk);
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);
        return island != null && island.hasPermission(superiorPlayer, BUILD) && island.isInsideRange(chunk);
    }

}
