package com.bgsoftware.wildloaders.handlers;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.scheduler.Scheduler;
import com.bgsoftware.wildloaders.utils.BlockPosition;
import com.bgsoftware.wildloaders.utils.database.Database;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

public final class DataHandler {

    private final WildLoadersPlugin plugin;

    public DataHandler(WildLoadersPlugin plugin) {
        this.plugin = plugin;
        Scheduler.runTask(() -> {
            try {
                Database.start(new File(plugin.getDataFolder(), "database.db"));
                loadDatabase();
            } catch (Exception ex) {
                ex.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }, 2L);
    }

    public void loadDatabase() {
        Database.executeUpdate("CREATE TABLE IF NOT EXISTS npc_identifiers (location TEXT NOT NULL PRIMARY KEY, uuid TEXT NOT NULL);");
        Database.executeQuery("SELECT * FROM npc_identifiers;", resultSet -> {
            while (resultSet.next()) {
                BlockPosition blockPosition = BlockPosition.deserialize(resultSet.getString("location"));
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                plugin.getNPCs().registerUUID(blockPosition, uuid);
            }
        });

        Database.executeUpdate("CREATE TABLE IF NOT EXISTS chunk_loaders (location TEXT NOT NULL PRIMARY KEY, placer TEXT NOT NULL, loader_data TEXT NOT NULL, timeLeft BIGINT NOT NULL);");
        Database.executeQuery("SELECT * FROM chunk_loaders;", resultSet -> {
            while (resultSet.next()) {
                BlockPosition blockPosition = BlockPosition.deserialize(resultSet.getString("location"));
                UUID placer = UUID.fromString(resultSet.getString("placer"));
                Optional<LoaderData> loaderData = plugin.getLoaders().getLoaderData(resultSet.getString("loader_data"));
                long timeLeft = resultSet.getLong("timeLeft");

                if (!loaderData.isPresent())
                    continue;

                World world = blockPosition.getWorld();
                if (world == null)
                    // Try loading the world
                    world = plugin.getProviders().loadWorld(blockPosition.getWorldName());

                if (world != null) {
                    Location location = blockPosition.getLocation();
                    if(Scheduler.isRegionScheduler()) {
                        Scheduler.runTask(location, () -> plugin.getLoaders().addChunkLoaderWithoutDBSave(
                                loaderData.get(), placer, location, timeLeft, true));
                    } else {
                        plugin.getLoaders().addChunkLoaderWithoutDBSave(loaderData.get(), placer,
                                location, timeLeft, true);
                    }
                } else {
                    plugin.getLoaders().addUnloadedChunkLoader(loaderData.get(), placer, blockPosition, timeLeft);
                }
            }
        });
    }

}
