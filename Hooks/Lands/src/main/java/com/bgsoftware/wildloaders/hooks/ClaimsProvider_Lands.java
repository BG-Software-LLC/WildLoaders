package com.bgsoftware.wildloaders.hooks;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.hooks.ClaimsProvider;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.role.enums.RoleSetting;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.UUID;

public final class ClaimsProvider_Lands implements ClaimsProvider {

    private final LandsIntegration landsIntegration;

    public ClaimsProvider_Lands(WildLoadersPlugin plugin) {
        landsIntegration = new LandsIntegration(plugin, false);
        landsIntegration.initialize();
    }

    @Override
    public boolean hasClaimAccess(UUID player, World world, int chunkX, int chunkZ) {
        Land land = landsIntegration.getLand(world, chunkX, chunkZ);
        return land == null || land.canSetting(player, RoleSetting.BLOCK_PLACE);
    }

}
