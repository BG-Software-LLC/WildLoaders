package com.bgsoftware.wildloaders.config;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.utils.items.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import java.util.*;
import java.util.stream.Collectors;

public class SettingsContainer {

    public List<String> hologramLines;
    public List<String> infiniteHologramLines;

    public final String databaseType;
    public final String databaseMySQLAddress;
    public final int databaseMySQLPort;
    public final String databaseMySQLDBName;
    public final String databaseMySQLUsername;
    public final String databaseMySQLPassword;
    public final String databaseMySQLPrefix;
    public final boolean databaseMySQLSSL;
    public final boolean databaseMySQLPublicKeyRetrieval;
    public final long databaseMySQLWaitTimeout;
    public final long databaseMySQLMaxLifetime;

    public SettingsContainer(WildLoadersPlugin plugin, YamlConfiguration config) {
        WildLoadersPlugin.log("Loading configuration started...");
        long startTime = System.currentTimeMillis();
        int loadersAmount = 0;

        hologramLines = config.getStringList("hologram-lines").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        infiniteHologramLines = config.getStringList("infinite-hologram-lines").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        Collections.reverse(this.hologramLines);
        Collections.reverse(this.infiniteHologramLines);

        databaseType = config.getString("database.type", "SQLite").toUpperCase(Locale.ENGLISH);
        databaseMySQLAddress = config.getString("database.address", "localhost");
        databaseMySQLPort = config.getInt("database.port", 3306);
        databaseMySQLDBName = config.getString("database.db-name", "wildloaders");
        databaseMySQLUsername = config.getString("database.user-name", "root");
        databaseMySQLPassword = config.getString("database.password", "");
        databaseMySQLPrefix = config.getString("database.prefix", "");
        databaseMySQLSSL = config.getBoolean("database.useSSL", false);
        databaseMySQLPublicKeyRetrieval = config.getBoolean("database.allowPublicKeyRetrieval", true);
        databaseMySQLWaitTimeout = config.getLong("database.waitTimeout", 600000);
        databaseMySQLMaxLifetime = config.getLong("database.maxLifetime", 1800000);

        plugin.getLoaders().removeLoadersData();

        for (String name : config.getConfigurationSection("chunkloaders").getKeys(false)) {
            ConfigurationSection loaderSection = config.getConfigurationSection("chunkloaders." + name);

            long timeLeft = loaderSection.getLong("time", Integer.MIN_VALUE);

            ItemBuilder itemBuilder = null;

            try {
                Material type = Material.valueOf(loaderSection.getString("type", ""));
                short data = (short) loaderSection.getInt("data", 0);

                itemBuilder = new ItemBuilder(type, data);

                if (loaderSection.contains("name"))
                    itemBuilder.setDisplayName(ChatColor.translateAlternateColorCodes('&', loaderSection.getString("name")));

                if (loaderSection.contains("lore")) {
                    List<String> lore = new ArrayList<>();

                    loaderSection.getStringList("lore").forEach(line ->
                            lore.add(ChatColor.translateAlternateColorCodes('&', line)));

                    itemBuilder.setLore(lore);
                }

                if (loaderSection.contains("enchants")) {
                    for (String line : loaderSection.getStringList("enchants")) {
                        Enchantment enchantment = Enchantment.getByName(line.split(":")[0]);
                        int level = Integer.parseInt(line.split(":")[1]);
                        itemBuilder.addEnchant(enchantment, level);
                    }
                }

                if (loaderSection.contains("skull")) {
                    itemBuilder.setTexture(loaderSection.getString("skull"));
                }
            } catch (Exception ignored) {
            }

            if (itemBuilder == null) {
                WildLoadersPlugin.log("Something went wrong while loading chunk-loader '" + name + "'.");
                continue;
            }

            LoaderData loaderData = plugin.getLoaders().createLoaderData(name, timeLeft, itemBuilder.build());

            if (loaderSection.contains("chunks-radius"))
                loaderData.setChunksRadius(loaderSection.getInt("chunks-radius"));

            if (loaderSection.contains("chunks-spread"))
                loaderData.setChunksSpread(loaderSection.getBoolean("chunks-spread"));

            loadersAmount++;
        }

        WildLoadersPlugin.log(" - Found " + loadersAmount + " chunk-loaders in config.yml.");
        WildLoadersPlugin.log("Loading configuration done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
    }

    


}
