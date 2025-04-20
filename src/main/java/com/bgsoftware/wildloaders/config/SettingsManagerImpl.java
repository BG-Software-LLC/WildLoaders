package com.bgsoftware.wildloaders.config;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.config.SettingsManager;
import com.bgsoftware.wildloaders.config.section.DatabaseSection;
import com.bgsoftware.wildloaders.config.section.GlobalSection;
import com.bgsoftware.wildloaders.errors.ManagerLoadException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class SettingsManagerImpl implements SettingsManager {

    private static final String[] IGNORED_SECTIONS = new String[]{ "chunkloaders" };

    private final WildLoadersPlugin plugin;

    private final GlobalSection global = new GlobalSection();
    private final DatabaseSection database = new DatabaseSection();

    private SettingsContainer container;

    public SettingsManagerImpl(WildLoadersPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadData() throws ManagerLoadException {
        File file = new File(plugin.getDataFolder(), "config.yml");

        if (!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        syncWithDefaultConfig(cfg, file);

        loadContainerFromConfig(cfg);
    }

    private void syncWithDefaultConfig(CommentedConfiguration cfg, File file) {
        try {
            cfg.syncWithConfig(file, plugin.getResource("config.yml"), IGNORED_SECTIONS);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to sync configuration file", ex);
        }
    }

    private void loadContainerFromConfig(YamlConfiguration config) {
        this.container = new SettingsContainer(plugin, config);
        this.global.setContainer(this.container);
        this.database.setContainer(container);
    }

    public SettingsContainer getContainer() {
        return container;
    }

    @Override
    public GlobalSection getGlobal() {
        return global;
    }

    @Override
    public DatabaseSection getDatabase() {
        return database;
    }
}
