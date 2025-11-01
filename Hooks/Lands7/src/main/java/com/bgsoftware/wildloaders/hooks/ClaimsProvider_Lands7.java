package com.bgsoftware.wildloaders.hooks;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.hooks.ClaimsProvider;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.flags.type.Flags;
import me.angeschossen.lands.api.land.Area;
import me.angeschossen.lands.api.land.Container;
import me.angeschossen.lands.api.land.LandWorld;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.UUID;

public final class ClaimsProvider_Lands7 implements ClaimsProvider {

    private static final boolean CHUNK_LOOKUP_SUPPORT = new ReflectMethod<Collection<?>>(
            Container.class, "getAreasInChunk", int.class, int.class, boolean.class)
            .isValid();

    private final LandsIntegration landsIntegration;

    public ClaimsProvider_Lands7(WildLoadersPlugin plugin) {
        landsIntegration = LandsIntegration.of(plugin);
    }

    @Override
    public boolean hasClaimAccess(UUID player, World world, int chunkX, int chunkZ) {
        return CHUNK_LOOKUP_SUPPORT ? canBuildChunkAreasCheck(player, world, chunkX, chunkZ) :
                canBuildLegacy(player, world, chunkX, chunkZ);
    }

    private boolean canBuildChunkAreasCheck(UUID player, World world, int chunkX, int chunkZ) {
        LandWorld landWorld = this.landsIntegration.getWorld(world);
        if (landWorld == null)
            return true;

        Container container = landWorld.getContainer(chunkX, chunkZ);
        if (container == null)
            return true;

        for (Area area : (Collection<Area>) container.getAreasInChunk(chunkX, chunkZ, true)) {
            if (!area.hasRoleFlag(player, Flags.BLOCK_PLACE))
                return false;
        }

        return true;
    }

    private boolean canBuildLegacy(UUID player, World world, int chunkX, int chunkZ) {
        Location chunkLocation = new Location(world, chunkX << 4, 100, chunkZ << 4);
        Area area = this.landsIntegration.getArea(chunkLocation);
        return area == null || area.hasRoleFlag(player, Flags.BLOCK_PLACE);
    }

}
