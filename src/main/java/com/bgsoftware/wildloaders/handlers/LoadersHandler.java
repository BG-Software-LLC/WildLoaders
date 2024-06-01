package com.bgsoftware.wildloaders.handlers;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.api.managers.LoadersManager;
import com.bgsoftware.wildloaders.loaders.UnloadedChunkLoader;
import com.bgsoftware.wildloaders.loaders.WChunkLoader;
import com.bgsoftware.wildloaders.loaders.WLoaderData;
import com.bgsoftware.wildloaders.utils.BlockPosition;
import com.bgsoftware.wildloaders.utils.ChunkLoaderChunks;
import com.bgsoftware.wildloaders.utils.ServerVersion;
import com.bgsoftware.wildloaders.utils.chunks.ChunkPosition;
import com.bgsoftware.wildloaders.utils.database.Query;
import com.google.common.collect.Maps;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class LoadersHandler implements LoadersManager {

    private final Map<BlockPosition, ChunkLoader> chunkLoaders = Maps.newConcurrentMap();
    private final Map<ChunkPosition, ChunkLoader> chunkLoadersByChunks = Maps.newConcurrentMap();
    private final Map<String, List<ChunkLoader>> chunkLoadersByWorlds = Maps.newConcurrentMap();
    private final Map<String, List<UnloadedChunkLoader>> unloadedChunkLoadersByWorlds = Maps.newConcurrentMap();
    private final Map<String, LoaderData> loadersData = Maps.newConcurrentMap();
    private final WildLoadersPlugin plugin;

    public LoadersHandler(WildLoadersPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Optional<ChunkLoader> getChunkLoader(Chunk chunk) {
        return Optional.ofNullable(chunkLoadersByChunks.get(ChunkPosition.of(chunk)));
    }

    @Override
    public Optional<ChunkLoader> getChunkLoader(Location location) {
        return Optional.ofNullable(chunkLoaders.get(BlockPosition.of(location)));
    }

    @Override
    public List<ChunkLoader> getChunkLoaders() {
        return Collections.unmodifiableList(new LinkedList<>(chunkLoaders.values()));
    }

    @Override
    public Optional<LoaderData> getLoaderData(String name) {
        return Optional.ofNullable(loadersData.get(name));
    }

    @Override
    public List<LoaderData> getLoaderDatas() {
        return new ArrayList<>(loadersData.values());
    }

    @Override
    public ChunkLoader addChunkLoader(LoaderData loaderData, Player whoPlaced, Location location, long timeLeft) {
        BlockPosition blockPosition = BlockPosition.of(location);

        WChunkLoader chunkLoader = addChunkLoaderWithoutDBSave(loaderData, whoPlaced.getUniqueId(),
                location, timeLeft, false);

        Query.INSERT_CHUNK_LOADER.insertParameters()
                .setLocation(blockPosition)
                .setObject(whoPlaced.getUniqueId().toString())
                .setObject(loaderData.getName())
                .setObject(timeLeft)
                .queue(blockPosition);

        return chunkLoader;
    }

    public WChunkLoader addChunkLoaderWithoutDBSave(LoaderData loaderData, UUID placer, Location location,
                                                    long timeLeft, boolean validateBlock) {
        BlockPosition blockPosition = BlockPosition.of(location);

        if (validateBlock) {
            Material blockType = location.getBlock().getType();

            if (ServerVersion.isLegacy() && blockType == Material.CAULDRON) {
                blockType = Material.CAULDRON_ITEM;
            }

            if (blockType != loaderData.getLoaderItem().getType()) {
                WildLoadersPlugin.log("The chunk-loader at " + blockPosition.serialize() + " is invalid.");
                return null;
            }
        }

        List<Chunk> loadedChunks = ChunkLoaderChunks.calculateChunks(loaderData, placer, location);
        loadedChunks.removeIf(chunk -> plugin.getLoaders().getChunkLoader(chunk).isPresent());

        WChunkLoader chunkLoader = new WChunkLoader(loaderData, placer, blockPosition, loadedChunks, timeLeft);
        chunkLoaders.put(blockPosition, chunkLoader);
        chunkLoadersByWorlds.computeIfAbsent(blockPosition.getWorldName(), i -> new LinkedList<>()).add(chunkLoader);
        for (Chunk loadedChunk : chunkLoader.getLoadedChunksCollection()) {
            chunkLoadersByChunks.put(ChunkPosition.of(loadedChunk), chunkLoader);
        }
        plugin.getNPCs().createNPC(blockPosition);
        return chunkLoader;
    }

    public void addUnloadedChunkLoader(LoaderData loaderData, UUID placer, BlockPosition blockPosition, long timeLeft) {
        UnloadedChunkLoader unloadedChunkLoader = new UnloadedChunkLoader(loaderData, placer, blockPosition, timeLeft);
        unloadedChunkLoadersByWorlds.computeIfAbsent(blockPosition.getWorldName(), i -> new LinkedList<>())
                .add(unloadedChunkLoader);
    }

    public void loadUnloadedChunkLoaders(World world) {
        List<UnloadedChunkLoader> unloadedChunkLoaders = this.unloadedChunkLoadersByWorlds.remove(world.getName());
        if (unloadedChunkLoaders == null || unloadedChunkLoaders.isEmpty())
            return;

        unloadedChunkLoaders.forEach(unloadedChunkLoader -> {
            Location location = unloadedChunkLoader.getBlockPosition().getLocation();
            if (location.getWorld() != world)
                throw new IllegalStateException();

            addChunkLoaderWithoutDBSave(unloadedChunkLoader.getLoaderData(), unloadedChunkLoader.getPlacer(),
                    location, unloadedChunkLoader.getTimeLeft(), true);
        });

    }

    public void unloadWorld(World world) {
        List<ChunkLoader> worldChunkLoaders = this.chunkLoadersByWorlds.remove(world.getName());
        if (worldChunkLoaders == null || worldChunkLoaders.isEmpty())
            return;

        List<UnloadedChunkLoader> unloadedChunkLoaders = new LinkedList<>();

        worldChunkLoaders.forEach(chunkLoader -> {
            plugin.getNMSAdapter().removeLoader(chunkLoader, false);
            BlockPosition blockPosition = removeChunkLoaderWithoutDBSave(chunkLoader);
            UnloadedChunkLoader unloadedChunkLoader = new UnloadedChunkLoader(chunkLoader.getLoaderData(),
                    chunkLoader.getWhoPlaced().getUniqueId(), blockPosition, chunkLoader.getTimeLeft());
            unloadedChunkLoaders.add(unloadedChunkLoader);

            Query.UPDATE_CHUNK_LOADER_TIME_LEFT.insertParameters()
                    .setObject(unloadedChunkLoader.getTimeLeft())
                    .setLocation(blockPosition)
                    .queue(blockPosition);
        });

        this.unloadedChunkLoadersByWorlds.put(world.getName(), unloadedChunkLoaders);
    }

    @Override
    public void removeChunkLoader(ChunkLoader chunkLoader) {
        BlockPosition blockPosition = removeChunkLoaderWithoutDBSave(chunkLoader);
        Query.DELETE_CHUNK_LOADER.insertParameters()
                .setLocation(blockPosition)
                .queue(blockPosition);
    }

    private BlockPosition removeChunkLoaderWithoutDBSave(ChunkLoader chunkLoader) {
        BlockPosition blockPosition = BlockPosition.of(chunkLoader.getLocation());
        chunkLoaders.remove(blockPosition);
        for (Chunk loadedChunk : chunkLoader.getLoadedChunksCollection()) {
            chunkLoadersByChunks.remove(ChunkPosition.of(loadedChunk));
        }

        List<ChunkLoader> worldChunkLoaders = chunkLoadersByWorlds.get(blockPosition.getWorldName());
        if (worldChunkLoaders != null)
            worldChunkLoaders.remove(chunkLoader);

        chunkLoader.getNPC().ifPresent(npc -> plugin.getNPCs().killNPC(npc));
        return blockPosition;
    }

    @Override
    public LoaderData createLoaderData(String name, long timeLeft, ItemStack itemStack) {
        LoaderData loaderData = new WLoaderData(name, timeLeft, itemStack);
        loadersData.put(name, loaderData);
        return loaderData;
    }

    @Override
    public void removeLoadersData() {
        loadersData.clear();
    }

    @Override
    public void removeChunkLoaders() {
        chunkLoaders.values().forEach(chunkLoader -> plugin.getNMSAdapter().removeLoader(chunkLoader, false));
        chunkLoaders.clear();
        chunkLoadersByChunks.clear();
        chunkLoadersByWorlds.clear();
    }
}
