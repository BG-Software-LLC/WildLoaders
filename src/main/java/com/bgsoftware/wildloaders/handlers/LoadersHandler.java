package com.bgsoftware.wildloaders.handlers;

import com.bgsoftware.common.databasebridge.sql.transaction.SQLDatabaseTransaction;
import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.api.managers.LoadersManager;
import com.bgsoftware.wildloaders.database.DBSession;
import com.bgsoftware.wildloaders.loaders.UnloadedChunkLoader;
import com.bgsoftware.wildloaders.loaders.WChunkLoader;
import com.bgsoftware.wildloaders.loaders.WLoaderData;
import com.bgsoftware.wildloaders.scheduler.Scheduler;
import com.bgsoftware.wildloaders.utils.BlockPosition;
import com.bgsoftware.wildloaders.utils.ChunkLoaderChunks;
import com.bgsoftware.wildloaders.utils.ServerVersion;
import com.bgsoftware.wildloaders.utils.SpawnerChangeListener;
import com.bgsoftware.wildloaders.utils.chunks.ChunkPosition;
import com.google.common.collect.Maps;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
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
        return getChunkLoader(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    @Override
    public Optional<ChunkLoader> getChunkLoader(World world, int chunkX, int chunkZ) {
        return Optional.ofNullable(chunkLoadersByChunks.get(ChunkPosition.of(world, chunkX, chunkZ)));
    }

    @Override
    public Optional<ChunkLoader> getChunkLoader(Location location) {
        return Optional.ofNullable(chunkLoaders.get(BlockPosition.of(location)));
    }

    public Optional<ChunkLoader> getChunkLoader(ChunkPosition chunkPosition) {
        return Optional.ofNullable(chunkLoadersByChunks.get(chunkPosition));
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
        return addChunkLoader(loaderData, whoPlaced, location, timeLeft, null);
    }

    public ChunkLoader addChunkLoader(LoaderData loaderData, Player whoPlaced, Location location, long timeLeft,
                                      @Nullable List<ChunkPosition> chunksToLoad) {
        WChunkLoader chunkLoader = addChunkLoaderWithoutDBSave(loaderData, whoPlaced.getUniqueId(),
                location, timeLeft, false, chunksToLoad);

        if (chunkLoader != null)
            plugin.getDataHandler().insertChunkLoader(chunkLoader);

        return chunkLoader;
    }

    public WChunkLoader addChunkLoaderWithoutDBSave(LoaderData loaderData, UUID placer, Location location,
                                                    long timeLeft, boolean validateBlock,
                                                    @Nullable List<ChunkPosition> chunksToLoad) {
        if (!plugin.isEnabled())
            return null;

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

        boolean filterLoadedChunks = chunksToLoad == null;
        if (filterLoadedChunks)
            chunksToLoad = ChunkLoaderChunks.calculateChunks(loaderData, placer, location);

        ChunkLoader existingChunkLoader = chunkLoaders.get(blockPosition);
        if (existingChunkLoader != null) {
            removeChunkLoader(existingChunkLoader, false);
            ChunkLoader replacement = chunkLoaders.get(blockPosition);
            if (replacement != null && replacement != existingChunkLoader)
                return (WChunkLoader) replacement;
        }

        if (filterLoadedChunks)
            chunksToLoad.removeIf(chunkPosition -> {
                ChunkLoader chunkLoader = chunkLoadersByChunks.get(chunkPosition);
                return chunkLoader != null && chunkLoader != existingChunkLoader;
            });

        WChunkLoader chunkLoader = new WChunkLoader(loaderData, placer, blockPosition, chunksToLoad, timeLeft);
        chunkLoaders.put(blockPosition, chunkLoader);
        chunkLoadersByWorlds.computeIfAbsent(blockPosition.getWorldName(), i -> new LinkedList<>()).add(chunkLoader);
        for (ChunkPosition chunkPosition : chunkLoader.getChunkPositions()) {
            chunkLoadersByChunks.put(chunkPosition, chunkLoader);
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
                    location, unloadedChunkLoader.getTimeLeft(), true, null);
        });

    }

    public void unloadWorld(World world) {
        List<ChunkLoader> worldChunkLoaders = this.chunkLoadersByWorlds.remove(world.getName());
        if (worldChunkLoaders == null || worldChunkLoaders.isEmpty())
            return;

        List<UnloadedChunkLoader> unloadedChunkLoaders = new LinkedList<>();

        SQLDatabaseTransaction<?> updateTransaction = null;

        for (ChunkLoader chunkLoader : worldChunkLoaders) {
            BlockPosition blockPosition = removeChunkLoader(chunkLoader, false);
            if (blockPosition == null)
                continue;

            UnloadedChunkLoader unloadedChunkLoader = new UnloadedChunkLoader(chunkLoader.getLoaderData(),
                    chunkLoader.getWhoPlaced().getUniqueId(), blockPosition, chunkLoader.getTimeLeft());
            unloadedChunkLoaders.add(unloadedChunkLoader);

            updateTransaction = plugin.getDataHandler().saveLoaderTimeLeft(chunkLoader, updateTransaction);
        }

        if (updateTransaction != null)
            DBSession.execute(updateTransaction);

        this.unloadedChunkLoadersByWorlds.put(world.getName(), unloadedChunkLoaders);
    }

    @Override
    public void removeChunkLoader(ChunkLoader chunkLoader) {
        Scheduler.ensureMain(chunkLoader.getLocation(), () -> removeChunkLoader(chunkLoader, true));
    }

    @Nullable
    private BlockPosition removeChunkLoader(ChunkLoader chunkLoader, boolean removeBlock) {
        WChunkLoader loader = (WChunkLoader) chunkLoader;
        WChunkLoader.State state = loader.getState();
        if (state == WChunkLoader.State.REMOVING || state == WChunkLoader.State.REMOVED)
            return null;

        Location location = chunkLoader.getLocation();
        BlockPosition blockPosition = BlockPosition.of(location);
        loader.setState(state == WChunkLoader.State.LOADING ?
                WChunkLoader.State.REMOVED : WChunkLoader.State.REMOVING);

        try {
            plugin.getNMSAdapter().removeLoader(chunkLoader,
                    removeBlock && (chunkLoader.getTimeLeft() <= 0 || state == WChunkLoader.State.LOADING),
                    SpawnerChangeListener.CALLBACK);
        } catch (RuntimeException | Error error) {
            loader.setState(chunkLoaders.get(blockPosition) == chunkLoader ? state : WChunkLoader.State.REMOVED);
            throw error;
        }

        boolean registered = chunkLoaders.remove(blockPosition, chunkLoader);
        for (ChunkPosition chunkPosition : loader.getChunkPositions()) {
            chunkLoadersByChunks.remove(chunkPosition, chunkLoader);
        }

        loader.setState(WChunkLoader.State.REMOVED);

        List<ChunkLoader> worldChunkLoaders = chunkLoadersByWorlds.get(blockPosition.getWorldName());
        if (worldChunkLoaders != null)
            worldChunkLoaders.remove(chunkLoader);

        if (registered) {
            if (removeBlock) {
                plugin.getDataHandler().deleteChunkLoader(chunkLoader);
                if (!chunkLoaders.containsKey(blockPosition))
                    location.getBlock().setType(Material.AIR);
            }

            chunkLoader.getNPC().ifPresent(npc -> plugin.getNPCs().killNPC(npc));
        }

        return registered ? blockPosition : null;
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
        List<ChunkLoader> chunkLoaders = getChunkLoaders();
        this.chunkLoaders.clear();
        chunkLoadersByChunks.clear();
        chunkLoadersByWorlds.clear();
        chunkLoaders.forEach(chunkLoader -> removeChunkLoader(chunkLoader, false));
    }
}
