package com.bgsoftware.wildloaders;

import com.bgsoftware.common.dependencies.DependenciesManager;
import com.bgsoftware.common.nmsloader.INMSLoader;
import com.bgsoftware.common.nmsloader.NMSHandlersFactory;
import com.bgsoftware.common.nmsloader.NMSLoadException;
import com.bgsoftware.common.nmsloader.config.NMSConfiguration;
import com.bgsoftware.common.updater.Updater;
import com.bgsoftware.wildloaders.api.WildLoaders;
import com.bgsoftware.wildloaders.api.WildLoadersAPI;
import com.bgsoftware.wildloaders.command.CommandsHandler;
import com.bgsoftware.wildloaders.config.SettingsManagerImpl;
import com.bgsoftware.wildloaders.errors.ManagerLoadException;
import com.bgsoftware.wildloaders.handlers.DataHandler;
import com.bgsoftware.wildloaders.handlers.LoadersHandler;
import com.bgsoftware.wildloaders.handlers.NPCHandler;
import com.bgsoftware.wildloaders.handlers.ProvidersHandler;
import com.bgsoftware.wildloaders.listeners.BlocksListener;
import com.bgsoftware.wildloaders.listeners.ChunksListener;
import com.bgsoftware.wildloaders.listeners.PlayersListener;
import com.bgsoftware.wildloaders.nms.NMSAdapter;
import com.bgsoftware.wildloaders.scheduler.Scheduler;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public final class WildLoadersPlugin extends JavaPlugin implements WildLoaders {

    private final Updater updater = new Updater(this, "wildloaders");

    private static WildLoadersPlugin plugin;

    private SettingsManagerImpl settingsHandler;
    private LoadersHandler loadersHandler;
    private NPCHandler npcHandler;
    private DataHandler dataHandler;
    private ProvidersHandler providersHandler;

    private NMSAdapter nmsAdapter;

    private boolean shouldEnable = true;

    @Override
    public void onLoad() {
        plugin = this;
        Scheduler.initialize();

        DependenciesManager.inject(this);

        new Metrics(this, 21732);

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

        loadersHandler = new LoadersHandler(this);
        settingsHandler = new SettingsManagerImpl(this);

        try {
            settingsHandler.loadData();
        } catch (ManagerLoadException ex) {
            if (!ManagerLoadException.handle(ex)) {
                return;
            }
        }

        dataHandler = new DataHandler(this);
        npcHandler = new NPCHandler(this);
        providersHandler = new ProvidersHandler(this);


        getServer().getPluginManager().registerEvents(new BlocksListener(this), this);
        getServer().getPluginManager().registerEvents(new ChunksListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayersListener(this), this);

        CommandsHandler commandsHandler = new CommandsHandler(this);
        getCommand("loader").setExecutor(commandsHandler);
        getCommand("loader").setTabCompleter(commandsHandler);

        Locale.reload();

        if (updater.isOutdated()) {
            log("");
            log("A new version is available (v" + updater.getLatestVersion() + ")!");
            log("Version's description: \"" + updater.getVersionDescription() + "\"");
            log("");
        }

        log("******** ENABLE DONE ********");
    }

    @Override
    public void onDisable() {
        if (!shouldEnable)
            return;

        Scheduler.disable();
        loadersHandler.removeChunkLoaders();
        npcHandler.killAllNPCs();

        log("Clearing database...");
        //We need to close the connection
        dataHandler.clearDatabase();
    }

    private boolean loadNMSAdapter() {
        try {
            INMSLoader nmsLoader = NMSHandlersFactory.createNMSLoader(this, NMSConfiguration.forPlugin(this));
            this.nmsAdapter = nmsLoader.loadNMSHandler(NMSAdapter.class);

            return true;
        } catch (NMSLoadException error) {
            log("&cThe plugin doesn't support your minecraft version.");
            log("&cPlease try a different version.");
            error.printStackTrace();

            return false;
        }
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

    public SettingsManagerImpl getSettings() {
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

    public Updater getUpdater() {
        return updater;
    }

    public static void log(String message) {
        plugin.getLogger().info(message);
    }

    public static WildLoadersPlugin getPlugin() {
        return plugin;
    }

}
