package com.bgsoftware.wildloaders.handlers;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.utils.items.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class SettingsHandler {

    public List<String> hologramLines;

    public SettingsHandler(WildLoadersPlugin plugin){
        WildLoadersPlugin.log("Loading configuration started...");
        long startTime = System.currentTimeMillis();
        int loadersAmount = 0;
        File file = new File(plugin.getDataFolder(), "config.yml");

        if(!file.exists())
            plugin.saveResource("config.yml", false);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        hologramLines = cfg.getStringList("hologram-lines").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList());

        plugin.getLoaders().removeLoadersData();

        for (String name : cfg.getConfigurationSection("chunkloaders").getKeys(false)) {
            long timeLeft = cfg.getLong("chunkloaders." + name + ".time", 0);

            ItemBuilder itemBuilder = null;

            try{
                Material type = Material.valueOf(cfg.getString("chunkloaders." + name + ".type", ""));
                short data = (short) cfg.getInt("chunkloaders." + name + ".data", 0);

                itemBuilder = new ItemBuilder(type, data);

                if(cfg.contains("chunkloaders." + name + ".name"))
                    itemBuilder.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                            cfg.getString("chunkloaders." + name + ".name")));

                if(cfg.contains("chunkloaders." + name + ".lore")) {
                    List<String> lore = new ArrayList<>();

                    cfg.getStringList("chunkloaders." + name + ".lore")
                            .forEach(line -> lore.add(ChatColor.translateAlternateColorCodes('&', line)));

                    itemBuilder.setLore(lore);
                }

                if(cfg.contains("chunkloaders." + name + ".enchants")) {
                    for(String line : cfg.getStringList("chunkloaders." + name + ".enchants")){
                        Enchantment enchantment = Enchantment.getByName(line.split(":")[0]);
                        int level = Integer.parseInt(line.split(":")[1]);
                        itemBuilder.addEnchant(enchantment, level);
                    }
                }

                if(cfg.contains("chunkloaders." + name + ".skull")) {
                    itemBuilder.setTexture(cfg.getString("chunkloaders." + name + ".skull"));
                }
            } catch(Exception ignored){}

            if (timeLeft <= 0 || itemBuilder == null) {
                WildLoadersPlugin.log("Something went wrong while loading chunk-loader '" + name + "'.");
                continue;
            }

            plugin.getLoaders().createLoaderData(name, timeLeft, itemBuilder.build());
            loadersAmount++;
        }

        WildLoadersPlugin.log(" - Found " + loadersAmount + " chunk-loaders in config.yml.");
        WildLoadersPlugin.log("Loading configuration done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
    }

    public static void reload(){
        try{
            WildLoadersPlugin plugin = WildLoadersPlugin.getPlugin();
            Field settings = WildLoadersPlugin.class.getDeclaredField("settingsHandler");
            settings.setAccessible(true);
            settings.set(plugin, new SettingsHandler(plugin));
        } catch(NoSuchFieldException | IllegalAccessException ex){
            ex.printStackTrace();
        }
    }

}
