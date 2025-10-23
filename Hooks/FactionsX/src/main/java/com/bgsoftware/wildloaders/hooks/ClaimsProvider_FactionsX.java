package com.bgsoftware.wildloaders.hooks;

import com.bgsoftware.wildloaders.api.hooks.ClaimsProvider;
import net.prosavage.factionsx.manager.GridManager;
import net.prosavage.factionsx.persist.data.FactionsKt;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public final class ClaimsProvider_FactionsX implements ClaimsProvider {

    @Override
    public boolean hasClaimAccess(UUID player, World world, int chunkX, int chunkZ) {
        Location blockLocation = new Location(world, chunkX << 4, 100, chunkZ << 4);
        return GridManager.INSTANCE.getFactionAt(FactionsKt.getFLocation(blockLocation)).getFactionMembers().contains(player);
    }

}
