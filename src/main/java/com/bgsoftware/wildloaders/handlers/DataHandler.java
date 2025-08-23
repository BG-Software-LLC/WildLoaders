package com.bgsoftware.wildloaders.handlers;

import com.bgsoftware.common.databasebridge.sql.query.Column;
import com.bgsoftware.common.databasebridge.sql.query.QueryResult;
import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.database.sql.DBSession;
import com.bgsoftware.wildloaders.scheduler.Scheduler;
import com.bgsoftware.wildloaders.utils.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.ResultSet;
import java.util.*;

public final class DataHandler {

    private final WildLoadersPlugin plugin;


    public DataHandler(WildLoadersPlugin plugin) {
        this.plugin = plugin;
        Scheduler.runTask(() -> {
            if (!DBSession.createConnection(plugin)) {
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().disablePlugin(plugin));
                return;
            }
            loadDatabase();
        }, 2L);
    }

    public void clearDatabase() {
        DBSession.close();
    }

    private void loadDatabase() {
        DBSession.createTable("npc_identifiers",
                new Column("location", "LONG_UNIQUE_TEXT PRIMARY KEY"),
                new Column("uuid", "TEXT")
        );

        DBSession.createTable("chunk_loaders",
                new Column("location", "LONG_UNIQUE_TEXT PRIMARY KEY"),
                new Column("placer", "UUID"),
                new Column("loader_data", "TEXT"),
                new Column("timeLeft", "BIGINT")
        );

        loadNPCs();
        loadChunkLoaders();

    }

    private void loadNPCs() {
        DBSession.select("npc_identifiers", "", new QueryResult<ResultSet>()
                .onSuccess(resultSet -> {
                    while (resultSet.next()) {
                        BlockPosition blockPosition = BlockPosition.deserialize(resultSet.getString("location"));
                        UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                        plugin.getNPCs().registerUUID(blockPosition, uuid);
                    }
                })
                .onFail(Throwable::printStackTrace)
        );
    }

    private void loadChunkLoaders() {
        DBSession.select("chunk_loaders", "", new QueryResult<ResultSet>()
                .onSuccess(resultSet -> {
                    while (resultSet.next()) {
                        BlockPosition blockPosition = BlockPosition.deserialize(resultSet.getString("location"));
                        UUID placer = UUID.fromString(resultSet.getString("placer"));
                        Optional<LoaderData> loaderData = plugin.getLoaders().getLoaderData(resultSet.getString("loader_data"));
                        long timeLeft = resultSet.getLong("timeLeft");

                        if (!loaderData.isPresent())
                            continue;

                        World world = blockPosition.getWorld();
                        if (world == null)
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
                })
                .onFail(Throwable::printStackTrace)
        );
    }

}

