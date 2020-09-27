package com.bgsoftware.wildloaders.handlers;

import com.bgsoftware.wildloaders.api.hooks.ClaimsProvider;
import com.bgsoftware.wildloaders.api.managers.ProvidersManager;
import com.bgsoftware.wildloaders.hooks.ClaimsProvider_FactionsUUID;
import com.bgsoftware.wildloaders.hooks.ClaimsProvider_FactionsX;
import com.bgsoftware.wildloaders.hooks.ClaimsProvider_MassiveFactions;
import com.bgsoftware.wildloaders.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProvidersHandler implements ProvidersManager {

    private final List<ClaimsProvider> claimsProviders = new ArrayList<>();

    public ProvidersHandler(){
        Executor.sync(() -> {
            if(Bukkit.getPluginManager().isPluginEnabled("Factions")){
                if(Bukkit.getPluginManager().getPlugin("Factions").getDescription().getAuthors().contains("drtshock"))
                    claimsProviders.add(new ClaimsProvider_FactionsUUID());
                else
                    claimsProviders.add(new ClaimsProvider_MassiveFactions());
            }
            if(Bukkit.getPluginManager().isPluginEnabled("FactionsX")){
                claimsProviders.add(new ClaimsProvider_FactionsX());
            }
        });
    }

    @Override
    public void addClaimsProvider(ClaimsProvider claimsProvider) {
        claimsProviders.add(claimsProvider);
    }

    public boolean hasChunkAccess(UUID player, Chunk chunk){
        for(ClaimsProvider claimsProvider : claimsProviders) {
            if (claimsProvider.hasClaimAccess(player, chunk))
                return true;
        }

        return false;
    }

}
