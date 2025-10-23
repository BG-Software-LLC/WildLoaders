package com.bgsoftware.wildloaders.hooks;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.wildloaders.api.hooks.ClaimsProvider;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public final class ClaimsProvider_SuperiorSkyblock implements ClaimsProvider {

    private static final IslandPrivilege BUILD = IslandPrivilege.getByName("BUILD");

    @Override
    public boolean hasClaimAccess(UUID player, World world, int chunkX, int chunkZ) {
        Location location = new Location(world, chunkX << 4, 100, chunkZ << 4);
        Island island = SuperiorSkyblockAPI.getGrid().getIslandAt(location);
        SuperiorPlayer superiorPlayer = SuperiorSkyblockAPI.getPlayer(player);
        return island != null && island.hasPermission(superiorPlayer, BUILD) && island.isInsideRange(location);
    }

}
