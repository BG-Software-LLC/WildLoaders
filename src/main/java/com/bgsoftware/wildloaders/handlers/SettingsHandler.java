package com.bgsoftware.wildloaders.handlers;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.utils.items.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class SettingsHandler {

    public List<String> hologramLines;
    public List<String> infiniteHologramLines;

    public SettingsHandler(WildLoadersPlugin plugin) {
        WildLoadersPlugin.log("Loading configuration started...");
        long startTime = System.currentTimeMillis();
        int loadersAmount = 0;
        File file = new File(plugin.getDataFolder(), "config.yml");

        if (!file.exists())
            plugin.saveResource("config.yml", false);

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        try {
            cfg.syncWithConfig(file, plugin.getResource("config.yml"), "chunkloaders");
        } catch (IOException error) {
            error.printStackTrace();
        }

        hologramLines = cfg.getStringList("hologram-lines").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        infiniteHologramLines = cfg.getStringList("infinite-hologram-lines").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());
        Collections.reverse(this.hologramLines);
        Collections.reverse(this.infiniteHologramLines);

        plugin.getLoaders().removeLoadersData();

        for (String name : cfg.getConfigurationSection("chunkloaders").getKeys(false)) {
            ConfigurationSection loaderSection = cfg.getConfigurationSection("chunkloaders." + name);

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
