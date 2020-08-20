package com.bgsoftware.wildloaders;

import com.bgsoftware.wildloaders.api.WildLoaders;
import com.bgsoftware.wildloaders.command.CommandsHandler;
import com.bgsoftware.wildloaders.handlers.DataHandler;
import com.bgsoftware.wildloaders.handlers.LoadersHandler;
import com.bgsoftware.wildloaders.handlers.NPCHandler;
import com.bgsoftware.wildloaders.handlers.SettingsHandler;
import com.bgsoftware.wildloaders.listeners.BlocksListener;
import com.bgsoftware.wildloaders.listeners.ChunksListener;
import com.bgsoftware.wildloaders.metrics.Metrics;
import com.bgsoftware.wildloaders.nms.NMSAdapter;
import org.bukkit.plugin.java.JavaPlugin;

public final class WildLoadersPlugin extends JavaPlugin implements WildLoaders {

    private static WildLoadersPlugin plugin;

    private SettingsHandler settingsHandler;
    private LoadersHandler loadersHandler;
    private NPCHandler npcHandler;
    private DataHandler dataHandler;

    private NMSAdapter nmsAdapter;

    @Override
    public void onEnable() {
        plugin = this;
        new Metrics(this);

        log("******** ENABLE START ********");

        loadNMSAdapter();

        dataHandler = new DataHandler(this);
        loadersHandler = new LoadersHandler(this);
        npcHandler = new NPCHandler(this);
        settingsHandler = new SettingsHandler(this);

        getServer().getPluginManager().registerEvents(new BlocksListener(this), this);
        getServer().getPluginManager().registerEvents(new ChunksListener(this), this);

        CommandsHandler commandsHandler = new CommandsHandler(this);
        getCommand("loader").setExecutor(commandsHandler);
        getCommand("loader").setTabCompleter(commandsHandler);

        Locale.reload();

//        if(Updater.isOutdated()) {
//            log("");
//            log("A new version is available (v" + Updater.getLatestVersion() + ")!");
//            log("Version's description: \"" + Updater.getVersionDescription() + "\"");
//            log("");
//        }

        log("******** ENABLE DONE ********");
    }

    @Override
    public void onDisable() {
        dataHandler.saveDatabase();
        loadersHandler.removeChunkLoaders();
        npcHandler.killAllNPCs();
    }

    private void loadNMSAdapter(){
        String version = getServer().getClass().getPackage().getName().split("\\.")[3];
        try{
            nmsAdapter = (NMSAdapter) Class.forName("com.bgsoftware.wildloaders.nms.NMSAdapter_" + version).newInstance();
        } catch(ClassNotFoundException | InstantiationException | IllegalAccessException ex){
            log("Couldn't load up with an adapter " + version + ". Please contact @Ome_R");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    public SettingsHandler getSettings() {
        return settingsHandler;
    }

    @Override
    public LoadersHandler getLoaders() {
        return loadersHandler;
    }

    public NPCHandler getNPCs() {
        return npcHandler;
    }

    public NMSAdapter getNMSAdapter() {
        return nmsAdapter;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }

    public static void log(String message){
        plugin.getLogger().info(message);
    }

    public static WildLoadersPlugin getPlugin(){
        return plugin;
    }

}
