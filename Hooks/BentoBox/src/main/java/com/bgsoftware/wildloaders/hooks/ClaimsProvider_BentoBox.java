package com.bgsoftware.wildloaders.hooks;

import com.bgsoftware.wildloaders.api.hooks.ClaimsProvider;
import org.bukkit.Chunk;
import org.bukkit.Location;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.lists.Flags;
import world.bentobox.bentobox.managers.IslandsManager;

import java.util.UUID;

public final class ClaimsProvider_BentoBox implements ClaimsProvider {

    private final IslandsManager islandsManager;

    public ClaimsProvider_BentoBox() {
        this.islandsManager = BentoBox.getInstance().getIslands();
    }

    @Override
    public boolean hasClaimAccess(UUID player, Chunk chunk) {
        User bentoPlayer = BentoBox.getInstance().getPlayersManager().getUser(player);

        // There is no API method to get island at a specific chunk.
        // Therefore we need to check for islands in each corner of the chunk.

        Location cornerLocation = new Location(chunk.getWorld(), chunk.getX() << 4, 100, chunk.getZ() << 4);

        IslandAccess islandAccess = IslandAccess.WILDERNESS;

        // Min x, Min z
        if ((islandAccess = islandAccess.and(checkIslandAccess(bentoPlayer, cornerLocation))) == IslandAccess.DENIED)
            return false;

        // Max x, Min z
        cornerLocation.add(15, 0, 0);
        if ((islandAccess = islandAccess.and(checkIslandAccess(bentoPlayer, cornerLocation))) == IslandAccess.DENIED)
            return false;

        // Max x, Max z
        cornerLocation.add(0, 0, 15);
        if ((islandAccess = islandAccess.and(checkIslandAccess(bentoPlayer, cornerLocation))) == IslandAccess.DENIED)
            return false;

        // Min x, Max z
        cornerLocation.add(-15, 0, 0);
        if ((islandAccess = islandAccess.and(checkIslandAccess(bentoPlayer, cornerLocation))) == IslandAccess.DENIED)
            return false;

        // We only return true if one of the corners were inside an actual island.
        return islandAccess == IslandAccess.ALLOWED;
    }

    private IslandAccess checkIslandAccess(User bentoPlayer, Location location) {
        Island island = this.islandsManager.getIslandAt(location).orElse(null);
        if (island == null)
            return IslandAccess.WILDERNESS;

        return island.isAllowed(bentoPlayer, Flags.PLACE_BLOCKS) ? IslandAccess.ALLOWED : IslandAccess.DENIED;
    }

    enum IslandAccess {

        WILDERNESS,
        ALLOWED,
        DENIED;

        public IslandAccess and(IslandAccess other) {
            return other.ordinal() > this.ordinal() ? other : this;
        }

    }

}