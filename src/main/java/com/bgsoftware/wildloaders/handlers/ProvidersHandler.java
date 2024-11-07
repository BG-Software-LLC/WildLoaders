package com.bgsoftware.wildloaders.handlers;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.hooks.ClaimsProvider;
import com.bgsoftware.wildloaders.api.hooks.TickableProvider;
import com.bgsoftware.wildloaders.api.hooks.WorldsProvider;
import com.bgsoftware.wildloaders.api.managers.ProvidersManager;
import com.bgsoftware.wildloaders.scheduler.Scheduler;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class ProvidersHandler implements ProvidersManager {

    private final WildLoadersPlugin plugin;

    private final List<ClaimsProvider> claimsProviders = new LinkedList<>();
    private final List<TickableProvider> tickableProviders = new LinkedList<>();
    private final List<WorldsProvider> worldsProviders = new LinkedList<>();

    public ProvidersHandler(WildLoadersPlugin plugin) {
        this.plugin = plugin;
        loadWorldProviders();
        Scheduler.runTask(() -> {
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
            } else {
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
        if (Bukkit.getPluginManager().isPluginEnabled("Lands")) {
            Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_Lands");
            claimsProvider.ifPresent(this::addClaimsProvider);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("BentoBox")) {
            Optional<ClaimsProvider> claimsProvider = createInstance("ClaimsProvider_BentoBox");
            claimsProvider.ifPresent(this::addClaimsProvider);
        }
    }

    private void loadTickableProviders() {
        // Loading the tickable providers
        if (Bukkit.getPluginManager().isPluginEnabled("EpicSpawners")) {
            Plugin epicSpawners = Bukkit.getPluginManager().getPlugin("EpicSpawners");
            String version = epicSpawners.getDescription().getVersion();
            if (version.startsWith("6")) {
                Optional<TickableProvider> tickableProvider = createInstance("TickableProvider_EpicSpawners6");
                tickableProvider.ifPresent(this::addTickableProvider);
            } else if (version.startsWith("7")) {
                Optional<TickableProvider> tickableProvider = createInstance("TickableProvider_EpicSpawners7");
                tickableProvider.ifPresent(this::addTickableProvider);
            } else {
                Optional<TickableProvider> tickableProvider = createInstance("TickableProvider_EpicSpawners8");
                tickableProvider.ifPresent(this::addTickableProvider);
            }
        }
    }

    private void loadWorldProviders() {
        Optional<WorldsProvider> worldsProvider;

        try {
            Class.forName("com.infernalsuite.aswm.api.SlimePlugin");
            worldsProvider = createInstanceSilently("WorldsProvider_AdvancedSlimePaper");
        } catch (ClassNotFoundException ignored) {
            try {
                Class.forName("com.grinderwolf.swm.nms.world.AbstractSlimeNMSWorld");
                worldsProvider = createInstanceSilently("WorldsProvider_AdvancedSlimeWorldManager");
            } catch (Throwable error) {
                worldsProvider = createInstanceSilently("WorldsProvider_SlimeWorldManager");
            }
        }

        worldsProvider.ifPresent(this::addWorldsProvider);
    }

    @Override
    public void addClaimsProvider(ClaimsProvider claimsProvider) {
        Preconditions.checkNotNull(claimsProvider, "claimsProvider cannot be null");
        claimsProviders.add(claimsProvider);
    }

    @Override
    public void addTickableProvider(TickableProvider tickableProvider) {
        Preconditions.checkNotNull(tickableProvider, "tickableProvider cannot be null");
        tickableProviders.add(tickableProvider);
    }

    @Override
    public void addWorldsProvider(WorldsProvider worldsProvider) {
        Preconditions.checkNotNull(worldsProvider, "worldsProvider cannot be null");
        worldsProviders.add(worldsProvider);
    }

    public boolean hasChunkAccess(UUID player, Chunk chunk) {
        for (ClaimsProvider claimsProvider : claimsProviders) {
            if (claimsProvider.hasClaimAccess(player, chunk))
                return true;
        }

        return false;
    }

    public void tick(List<Chunk> chunks) {
        tickableProviders.forEach(tickableProvider -> tickableProvider.tick(chunks));
    }

    @Nullable
    public World loadWorld(String worldName) {
        for (WorldsProvider worldsProvider : this.worldsProviders) {
            World loadedWorld = worldsProvider.loadWorld(worldName);
            if (loadedWorld != null)
                return loadedWorld;
        }

        return null;
    }

    private <T> Optional<T> createInstanceSilently(String className) {
        try {
            return createInstance(className);
        } catch (Throwable error) {
            return Optional.empty();
        }
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
