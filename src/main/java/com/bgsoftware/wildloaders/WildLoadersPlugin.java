package com.bgsoftware.wildloaders;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.wildloaders.api.WildLoaders;
import com.bgsoftware.wildloaders.api.WildLoadersAPI;
import com.bgsoftware.wildloaders.command.CommandsHandler;
import com.bgsoftware.wildloaders.handlers.DataHandler;
import com.bgsoftware.wildloaders.handlers.LoadersHandler;
import com.bgsoftware.wildloaders.handlers.NPCHandler;
import com.bgsoftware.wildloaders.handlers.ProvidersHandler;
import com.bgsoftware.wildloaders.handlers.SettingsHandler;
import com.bgsoftware.wildloaders.listeners.BlocksListener;
import com.bgsoftware.wildloaders.listeners.ChunksListener;
import com.bgsoftware.wildloaders.listeners.PlayersListener;
import com.bgsoftware.wildloaders.metrics.Metrics;
import com.bgsoftware.wildloaders.nms.NMSAdapter;
import com.bgsoftware.wildloaders.utils.Pair;
import com.bgsoftware.wildloaders.utils.ServerVersion;
import com.bgsoftware.wildloaders.utils.database.Database;
import org.bukkit.Bukkit;
import org.bukkit.UnsafeValues;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public final class WildLoadersPlugin extends JavaPlugin implements WildLoaders {

    private static WildLoadersPlugin plugin;

    private SettingsHandler settingsHandler;
    private LoadersHandler loadersHandler;
    private NPCHandler npcHandler;
    private DataHandler dataHandler;
    private ProvidersHandler providersHandler;

    private NMSAdapter nmsAdapter;

    private boolean shouldEnable = true;

    @Override
    public void onLoad() {
        plugin = this;
        new Metrics(this);

        shouldEnable = loadNMSAdapter();
        loadAPI();

        if (!shouldEnable)
            log("&cThere was an error while loading the plugin.");
    }

    @Override
    public void onEnable() {
        if (!shouldEnable) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        log("******** ENABLE START ********");

        dataHandler = new DataHandler(this);
        loadersHandler = new LoadersHandler(this);
        npcHandler = new NPCHandler(this);
        providersHandler = new ProvidersHandler(this);
        settingsHandler = new SettingsHandler(this);

        getServer().getPluginManager().registerEvents(new BlocksListener(this), this);
        getServer().getPluginManager().registerEvents(new ChunksListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayersListener(this), this);

        CommandsHandler commandsHandler = new CommandsHandler(this);
        getCommand("loader").setExecutor(commandsHandler);
        getCommand("loader").setTabCompleter(commandsHandler);

        Locale.reload();

        if (Updater.isOutdated()) {
            log("");
            log("A new version is available (v" + Updater.getLatestVersion() + ")!");
            log("Version's description: \"" + Updater.getVersionDescription() + "\"");
            log("");
        }

        log("******** ENABLE DONE ********");
    }

    @Override
    public void onDisable() {
        if (shouldEnable) {
            Database.stop();
            loadersHandler.removeChunkLoaders();
            npcHandler.killAllNPCs();
        }
    }

    private boolean loadNMSAdapter() {
        String version = null;

        if (ServerVersion.isLessThan(ServerVersion.v1_17)) {
            version = getServer().getClass().getPackage().getName().split("\\.")[3];
        } else {
            ReflectMethod<Integer> getDataVersion = new ReflectMethod<>(UnsafeValues.class, "getDataVersion");
            int dataVersion = getDataVersion.invoke(Bukkit.getUnsafe());

            List<Pair<Integer, String>> versions = Arrays.asList(
                    new Pair<>(2729, null),
                    new Pair<>(2730, "v1_17"),
                    new Pair<>(2974, null),
                    new Pair<>(2975, "v1_18"),
                    new Pair<>(3336, null),
                    new Pair<>(3337, "v1_19"),
                    new Pair<>(3465, "v1_20_1")
            );

            for (Pair<Integer, String> versionData : versions) {
                if (dataVersion <= versionData.first) {
                    version = versionData.second;
                    break;
                }
            }

            if (version == null) {
                log("Data version: " + dataVersion);
            }
        }

        if (version != null) {
            try {
                nmsAdapter = (NMSAdapter) Class.forName(String.format("com.bgsoftware.wildloaders.nms.%s.NMSAdapter", version)).newInstance();
                return true;
            } catch (Exception error) {
                error.printStackTrace();
            }
        }

        log("&cThe plugin doesn't support your minecraft version.");
        log("&cPlease try a different version.");

        return false;
    }

    private void loadAPI() {
        try {
            Field instance = WildLoadersAPI.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, this);
        } catch (Exception ex) {
            log("Failed to set-up API - disabling plugin...");
            ex.printStackTrace();
            shouldEnable = false;
        }
    }

    public SettingsHandler getSettings() {
        return settingsHandler;
    }

    @Override
    public LoadersHandler getLoaders() {
        return loadersHandler;
    }

    @Override
    public NPCHandler getNPCs() {
        return npcHandler;
    }

    @Override
    public ProvidersHandler getProviders() {
        return providersHandler;
    }

    public NMSAdapter getNMSAdapter() {
        return nmsAdapter;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }

    public static void log(String message) {
        plugin.getLogger().info(message);
    }

    public static WildLoadersPlugin getPlugin() {
        return plugin;
    }

}
