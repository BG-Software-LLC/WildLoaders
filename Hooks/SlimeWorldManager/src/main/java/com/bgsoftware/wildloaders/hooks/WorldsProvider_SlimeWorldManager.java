package com.bgsoftware.wildloaders.hooks;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.hooks.WorldsProvider;
import com.grinderwolf.swm.api.SlimePlugin;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Arrays;
import java.util.List;

public class WorldsProvider_SlimeWorldManager implements WorldsProvider {

    private static final List<String> WORLD_LOADERS = Arrays.asList("file", "mysql", "mongodb", "redis", "api");
    private static final SlimePropertyMap EMPTY_PROPERTIES = new SlimePropertyMap();

    private final SlimePlugin slimePlugin;

    public WorldsProvider_SlimeWorldManager() {
        this.slimePlugin = (SlimePlugin) Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
    }

    @Override
    public World loadWorld(String worldName) {
        for (String loaderName : WORLD_LOADERS) {
            SlimeLoader slimeLoader = this.slimePlugin.getLoader(loaderName);
            try {
                if (slimeLoader != null && slimeLoader.worldExists(worldName)) {
                    SlimeWorld slimeWorld = slimePlugin.loadWorld(slimeLoader, worldName, false, EMPTY_PROPERTIES);
                    if (slimeWorld != null) {
                        World bukkitWorld = Bukkit.getWorld(slimeWorld.getName());
                        if (bukkitWorld != null)
                            return bukkitWorld;
                    }
                }
            } catch (Exception error) {
                WildLoadersPlugin.log("An error occurred while trying to load world " + worldName);
                error.printStackTrace();
            }
        }

        return null;
    }

}
