package com.bgsoftware.wildloaders.handlers;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC;
import com.bgsoftware.wildloaders.loaders.WChunkLoader;
import com.bgsoftware.wildloaders.npc.NPCIdentifier;
import com.bgsoftware.wildloaders.utils.database.Query;
import com.bgsoftware.wildloaders.utils.database.SQLHelper;
import com.bgsoftware.wildloaders.utils.database.StatementHolder;
import com.bgsoftware.wildloaders.utils.locations.LocationUtils;
import com.bgsoftware.wildloaders.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class DataHandler {

    private final WildLoadersPlugin plugin;

    public DataHandler(WildLoadersPlugin plugin){
        this.plugin = plugin;
        Executor.sync(() -> {
            try {
                SQLHelper.init(new File(plugin.getDataFolder(), "database.db"));
                loadDatabase();
            }catch (Exception ex){
                ex.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }, 2L);
    }

    public void loadDatabase(){
        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS npc_identifiers (location TEXT NOT NULL PRIMARY KEY, uuid TEXT NOT NULL);");
        SQLHelper.executeQuery("SELECT * FROM npc_identifiers;", resultSet -> {
            while (resultSet.next()) {
                Location location = LocationUtils.getLocation(resultSet.getString("location"));
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                plugin.getNPCs().registerUUID(location, uuid);
            }
        });

        SQLHelper.executeUpdate("CREATE TABLE IF NOT EXISTS chunk_loaders (location TEXT NOT NULL PRIMARY KEY, placer TEXT NOT NULL, loader_data TEXT NOT NULL, timeLeft BIGINT NOT NULL);");
        SQLHelper.executeQuery("SELECT * FROM chunk_loaders;", resultSet -> {
            while(resultSet.next()){
                Location location = LocationUtils.getLocation(resultSet.getString("location"));
                UUID placer = UUID.fromString(resultSet.getString("placer"));
                Optional<LoaderData> loaderData = plugin.getLoaders().getLoaderData(resultSet.getString("loader_data"));
                long timeLeft = resultSet.getLong("timeLeft");

                if(!loaderData.isPresent())
                    continue;

                if(location.getBlock().getType() != loaderData.get().getLoaderItem().getType()){
                    WildLoadersPlugin.log("The chunk-loader at " + LocationUtils.getLocation(location) + " is invalid.");
                    continue;
                }

                plugin.getLoaders().addChunkLoader(loaderData.get(), placer, location, timeLeft);
            }
        });
    }

    public void saveDatabase(){
        List<ChunkLoader> chunkLoaderList = plugin.getLoaders().getChunkLoaders();
        Map<NPCIdentifier, ChunkLoaderNPC> chunkLoaderNPCList = plugin.getNPCs().getNPCs();

        {
            StatementHolder chunkLoadersHolder = Query.INSERT_CHUNK_LOADER.getStatementHolder();
            chunkLoadersHolder.prepareBatch();
            chunkLoaderList.stream().filter(chunkLoader -> chunkLoader.getTimeLeft() > 0).forEach(chunkLoader ->
                    ((WChunkLoader) chunkLoader).updateInsertStatement(chunkLoadersHolder).addBatch());
            chunkLoadersHolder.execute(false);
        }

        {
            StatementHolder chunkLoadersHolder = Query.DELETE_CHUNK_LOADER.getStatementHolder();
            chunkLoadersHolder.prepareBatch();
            chunkLoaderList.stream().filter(chunkLoader -> chunkLoader.getTimeLeft() <= 0).forEach(chunkLoader ->
                    chunkLoadersHolder.setLocation(chunkLoader.getLocation()).addBatch());
            chunkLoadersHolder.execute(false);
        }

        {
            StatementHolder npcIdentifierHolder = Query.INSERT_NPC_IDENTIFIER.getStatementHolder();
            npcIdentifierHolder.prepareBatch();
            chunkLoaderNPCList.forEach((identifier, npc) -> npcIdentifierHolder
                            .setLocation(identifier.getSpawnLocation()).setString(npc.getUniqueId().toString()).addBatch());
            npcIdentifierHolder.execute(false);
        }

    }

    public void saveChunkLoadersTimes(){
        List<ChunkLoader> chunkLoaderList = plugin.getLoaders().getChunkLoaders();

        {
            StatementHolder chunkLoadersHolder = Query.UPDATE_CHUNK_LOADER_TIME_LEFT.getStatementHolder();
            chunkLoadersHolder.prepareBatch();
            chunkLoaderList.stream().filter(chunkLoader -> chunkLoader.getTimeLeft() > 0).forEach(chunkLoader ->
                    chunkLoadersHolder.setLong(chunkLoader.getTimeLeft()).setLocation(chunkLoader.getLocation()).addBatch());
            chunkLoadersHolder.execute(false);
        }

        {
            StatementHolder chunkLoadersHolder = Query.DELETE_CHUNK_LOADER.getStatementHolder();
            chunkLoadersHolder.prepareBatch();
            chunkLoaderList.stream().filter(chunkLoader -> chunkLoader.getTimeLeft() <= 0).forEach(chunkLoader ->
                    chunkLoadersHolder.setLocation(chunkLoader.getLocation()).addBatch());
            chunkLoadersHolder.execute(false);
        }
    }

}
