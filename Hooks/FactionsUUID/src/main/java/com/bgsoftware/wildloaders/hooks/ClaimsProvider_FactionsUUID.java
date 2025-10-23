package com.bgsoftware.wildloaders.hooks;

import com.bgsoftware.wildloaders.api.hooks.ClaimsProvider;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.UUID;

public final class ClaimsProvider_FactionsUUID implements ClaimsProvider {

    @Override
    public boolean hasClaimAccess(UUID player, World world, int chunkX, int chunkZ) {
        FPlayer fPlayer = FPlayers.getInstance().getById(player.toString());
        FLocation fLocation = new FLocation(world.getName(), chunkX, chunkZ);
        Faction faction = Board.getInstance().getFactionAt(fLocation);
        return !faction.isWilderness() && faction.getFPlayers().contains(fPlayer);
    }

}
