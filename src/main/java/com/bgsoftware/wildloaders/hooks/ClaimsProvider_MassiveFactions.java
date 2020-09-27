package com.bgsoftware.wildloaders.hooks;

import com.bgsoftware.wildloaders.api.hooks.ClaimsProvider;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.Chunk;

import java.util.UUID;

public final class ClaimsProvider_MassiveFactions implements ClaimsProvider {

    @Override
    public boolean hasClaimAccess(UUID player, Chunk chunk) {
        MPlayer mPlayer = MPlayer.get(player);
        Faction faction = BoardColl.get().getFactionAt(PS.valueOf(chunk));
        return !faction.getId().equals(Factions.ID_NONE) && faction.getMPlayers().contains(mPlayer);
    }

}
