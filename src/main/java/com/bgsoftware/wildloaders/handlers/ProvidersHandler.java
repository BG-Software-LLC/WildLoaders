package com.bgsoftware.wildloaders.handlers;

import com.bgsoftware.wildloaders.api.hooks.ClaimsProvider;
import com.bgsoftware.wildloaders.api.hooks.TickableProvider;
import com.bgsoftware.wildloaders.api.managers.ProvidersManager;
import com.bgsoftware.wildloaders.hooks.ClaimsProvider_FactionsUUID;
import com.bgsoftware.wildloaders.hooks.ClaimsProvider_FactionsX;
import com.bgsoftware.wildloaders.hooks.ClaimsProvider_MassiveFactions;
import com.bgsoftware.wildloaders.hooks.ClaimsProvider_SuperiorSkyblock;
import com.bgsoftware.wildloaders.hooks.TickableProvider_EpicSpawners;
import com.bgsoftware.wildloaders.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProvidersHandler implements ProvidersManager {

    private final List<ClaimsProvider> claimsProviders = new ArrayList<>();
    private final List<TickableProvider> tickableProviders = new ArrayList<>();

    public ProvidersHandler(){
        Executor.sync(() -> {
            // Loading the claim providers
            if(Bukkit.getPluginManager().isPluginEnabled("Factions")){
                if(Bukkit.getPluginManager().getPlugin("Factions").getDescription().getAuthors().contains("drtshock"))
                    addClaimsProvider(new ClaimsProvider_FactionsUUID());
                else
                    addClaimsProvider(new ClaimsProvider_MassiveFactions());
            }
            if(Bukkit.getPluginManager().isPluginEnabled("FactionsX")){
                addClaimsProvider(new ClaimsProvider_FactionsX());
            }
            if(Bukkit.getPluginManager().isPluginEnabled("SuperiorSkyblock2")){
                addClaimsProvider(new ClaimsProvider_SuperiorSkyblock());
            }

            // Loading the tickable providers
            if(Bukkit.getPluginManager().isPluginEnabled("EpicSpawners")){
                addTickableProvider(new TickableProvider_EpicSpawners());
            }
        });
    }

    @Override
    public void addClaimsProvider(ClaimsProvider claimsProvider) {
        claimsProviders.add(claimsProvider);
    }

    @Override
    public void addTickableProvider(TickableProvider tickableProvider) {
        tickableProviders.add(tickableProvider);
    }

    public boolean hasChunkAccess(UUID player, Chunk chunk){
        for(ClaimsProvider claimsProvider : claimsProviders) {
            if (claimsProvider.hasClaimAccess(player, chunk))
                return true;
        }

        return false;
    }

    public void tick(Chunk[] chunks){
        tickableProviders.forEach(tickableProvider -> tickableProvider.tick(chunks));
    }

}
