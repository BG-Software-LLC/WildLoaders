package com.bgsoftware.wildloaders.hooks;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.hooks.ClaimsProvider;
import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import me.angeschossen.lands.api.role.enums.RoleSetting;
import org.bukkit.Chunk;

import java.util.UUID;

public final class ClaimsProvider_Lands implements ClaimsProvider {

    private final LandsIntegration landsIntegration;

    public ClaimsProvider_Lands(WildLoadersPlugin plugin) {
        landsIntegration = new LandsIntegration(plugin, false);
        landsIntegration.initialize();
    }

    @Override
    public boolean hasClaimAccess(UUID player, Chunk chunk) {
        Land land = landsIntegration.getLand(chunk.getWorld(), chunk.getX(), chunk.getZ());
        return land == null || land.canSetting(player, RoleSetting.BLOCK_PLACE);
    }

}
