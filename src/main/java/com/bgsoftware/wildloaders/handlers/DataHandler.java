package com.bgsoftware.wildloaders.handlers;

import com.bgsoftware.common.databasebridge.sql.query.Column;
import com.bgsoftware.common.databasebridge.sql.query.QueryResult;
import com.bgsoftware.common.databasebridge.sql.transaction.DeleteSQLDatabaseTransaction;
import com.bgsoftware.common.databasebridge.sql.transaction.InsertSQLDatabaseTransaction;
import com.bgsoftware.common.databasebridge.sql.transaction.SQLDatabaseTransaction;
import com.bgsoftware.common.databasebridge.sql.transaction.UpdateSQLDatabaseTransaction;
import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC;
import com.bgsoftware.wildloaders.database.DBSession;
import com.bgsoftware.wildloaders.scheduler.Scheduler;
import com.bgsoftware.wildloaders.utils.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public final class DataHandler {

    private final WildLoadersPlugin plugin;

    public DataHandler(WildLoadersPlugin plugin) {
        this.plugin = plugin;
        Scheduler.runTask(() -> {
            try {
                if (!DBSession.createConnection(plugin)) {
                    WildLoadersPlugin.log("Cannot connect to database, stopping plugin...");
                    Bukkit.getPluginManager().disablePlugin(plugin);
                }

                loadDatabase();
            } catch (Exception ex) {
                ex.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }, 2L);
    }

    public void loadDatabase() {
        DBSession.createTable("npc_identifiers",
                new Column("location", "LONG_UNIQUE_TEXT NOT NULL PRIMARY KEY"),
                new Column("uuid", "UUID NOT NULL"));
        DBSession.createTable("chunk_loaders",
                new Column("location", "LONG_UNIQUE_TEXT NOT NULL PRIMARY KEY"),
                new Column("placer", "UUID NOT NULL"),
                new Column("loader_data", "TEXT NOT NULL"),
                new Column("timeLeft", "BIGINT NOT NULL"));

        DBSession.select("npc_identifiers", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
            while (resultSet.next()) {
                BlockPosition blockPosition = BlockPosition.deserialize(resultSet.getString("location"));
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                plugin.getNPCs().registerUUID(blockPosition, uuid);
            }
        }).onFail(Throwable::printStackTrace));

        DBSession.select("chunk_loaders", "", new QueryResult<ResultSet>().onSuccess(resultSet -> {
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
                    Scheduler.runTaskLater(() -> {
                    plugin.getLoaders().addChunkLoaderWithoutDBSave(
                            loaderData.get(), placer, location, timeLeft, true, null);
                    }, 1L);
                } else {
                    plugin.getLoaders().addUnloadedChunkLoader(loaderData.get(), placer, blockPosition, timeLeft);
                }
            }
        }).onFail(Throwable::printStackTrace));
    }

    public void insertChunkLoader(ChunkLoader chunkLoader) {
        DBSession.execute(new InsertSQLDatabaseTransaction("chunk_loaders",
                Arrays.asList("location", "placer", "loader_data", "timeLeft"))
                .bindObject(serializeLocationInternal(chunkLoader.getLocation()))
                .bindObject(chunkLoader.getWhoPlaced().getUniqueId().toString())
                .bindObject(chunkLoader.getLoaderData().getName())
                .bindObject(chunkLoader.getTimeLeft())
        );
    }

    public SQLDatabaseTransaction<?> saveLoaderTimeLeft(ChunkLoader chunkLoader, @Nullable SQLDatabaseTransaction<?> transaction) {
        if (transaction == null) {
            // UPDATE  SET =? WHERE =?;
            transaction = new UpdateSQLDatabaseTransaction("chunk_loaders",
                    Arrays.asList("timeLeft"), Arrays.asList("location"));
        }

        transaction
                .bindObject(chunkLoader.getTimeLeft())
                .bindObject(serializeLocationInternal(chunkLoader.getLocation()))
                .newBatch();

        return transaction;
    }

    public void saveLoaderTimeLeft(ChunkLoader chunkLoader) {
        DBSession.execute(saveLoaderTimeLeft(chunkLoader, null));
    }

    public void deleteChunkLoader(ChunkLoader chunkLoader) {
        DBSession.execute(new DeleteSQLDatabaseTransaction("chunk_loaders",
                Arrays.asList("location"))
                .bindObject(serializeLocationInternal(chunkLoader.getLocation()))
        );
    }

    public void deleteNPC(ChunkLoaderNPC npc) {
        DBSession.execute(new DeleteSQLDatabaseTransaction("npc_identifiers",
                Arrays.asList("location"))
                .bindObject(serializeLocationInternal(npc.getLocation()))
        );
    }

    public void insertNPC(ChunkLoaderNPC npc) {
        DBSession.execute(new InsertSQLDatabaseTransaction("npc_identifiers",
                Arrays.asList("location", "uuid"))
                .bindObject(serializeLocationInternal(npc.getLocation()))
                .bindObject(npc.getUniqueId().toString())
        );
    }

    private static String serializeLocationInternal(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," +
                location.getBlockY() + "," + location.getBlockZ();
    }

}
