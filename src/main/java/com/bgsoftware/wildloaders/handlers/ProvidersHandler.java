package com.bgsoftware.wildloaders.handlers;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.hooks.ClaimsProvider;
import com.bgsoftware.wildloaders.api.hooks.TickableProvider;
import com.bgsoftware.wildloaders.api.managers.ProvidersManager;
import com.bgsoftware.wildloaders.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ProvidersHandler implements ProvidersManager {

    private final WildLoadersPlugin plugin;

    private final List<ClaimsProvider> claimsProviders = new ArrayList<>();
    private final List<TickableProvider> tickableProviders = new ArrayList<>();

    public ProvidersHandler(WildLoadersPlugin plugin) {
        this.plugin = plugin;
        Executor.sync(() -> {
            loadClaimsProviders();
            loadTickableProviders();
        });
    }

    private void loadClaimsProviders() {
        // Loading the claim providers
        if (Bukkit.getPluginManager().isPluginEnabled("Factions")) {
            if (Bukkit.getPluginManager().getPlugin("Factions").getDescription().getAuthors().contains("drtshock")) {
                Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_FactionsUUID");
                claimsProvider.ifPresent(this::addClaimsProvider);
            }
            else {
                Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_MassiveFactions");
                claimsProvider.ifPresent(this::addClaimsProvider);
            }
        }
        if (Bukkit.getPluginManager().isPluginEnabled("FactionsX")) {
            Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_FactionsX");
            claimsProvider.ifPresent(this::addClaimsProvider);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("SuperiorSkyblock2")) {
            Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_SuperiorSkyblock");
            claimsProvider.ifPresent(this::addClaimsProvider);
        }
    }

    private void loadTickableProviders() {
        // Loading the tickable providers
        if (Bukkit.getPluginManager().isPluginEnabled("EpicSpawners")) {
            Optional<TickableProvider> tickableProvider = createInstance("TickableProvider_EpicSpawners");
            tickableProvider.ifPresent(this::addTickableProvider);
        }
    }

    @Override
    public void addClaimsProvider(ClaimsProvider claimsProvider) {
        claimsProviders.add(claimsProvider);
    }

    @Override
    public void addTickableProvider(TickableProvider tickableProvider) {
        tickableProviders.add(tickableProvider);
    }

    public boolean hasChunkAccess(UUID player, Chunk chunk) {
        for (ClaimsProvider claimsProvider : claimsProviders) {
            if (claimsProvider.hasClaimAccess(player, chunk))
                return true;
        }

        return false;
    }

    public void tick(Chunk[] chunks) {
        tickableProviders.forEach(tickableProvider -> tickableProvider.tick(chunks));
    }

    private <T> Optional<T> createInstance(String className) {
        try {
            Class<?> clazz = Class.forName("com.bgsoftware.wildloaders.hooks." + className);
            try {
                Method compatibleMethod = clazz.getDeclaredMethod("isCompatible");
                if (!(boolean) compatibleMethod.invoke(null))
                    return Optional.empty();
            } catch (Exception ignored) {
            }

            try {
                Constructor<?> constructor = clazz.getConstructor(WildLoadersPlugin.class);
                // noinspection unchecked
                return Optional.of((T) constructor.newInstance(plugin));
            } catch (Exception error) {
                // noinspection unchecked
                return Optional.of((T) clazz.newInstance());
            }
        } catch (ClassNotFoundException ignored) {
            return Optional.empty();
        } catch (Exception error) {
            error.printStackTrace();
            return Optional.empty();
        }
    }

}
