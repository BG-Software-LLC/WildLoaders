package com.bgsoftware.wildloaders.hooks;

import com.bgsoftware.wildloaders.api.hooks.ClaimsProvider;
import net.prosavage.factionsx.manager.GridManager;
import net.prosavage.factionsx.persist.data.FactionsKt;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.UUID;

public final class ClaimsProvider_FactionsX implements ClaimsProvider {

    @Override
    public boolean hasClaimAccess(UUID player, Chunk chunk) {
        Location blockLocation = new Location(chunk.getWorld(), chunk.getX() << 4, 100, chunk.getZ() << 4);
        return GridManager.INSTANCE.getFactionAt(FactionsKt.getFLocation(blockLocation)).getFactionMembers().contains(player);
    }

}
