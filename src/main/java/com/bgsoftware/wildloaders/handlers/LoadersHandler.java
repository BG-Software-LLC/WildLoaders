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
        // Preventing the async callback of chunk loads to call this after the plugin was disabled.
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

        // Only one loader per block: unload the old one before its chunks are filtered out below.
        // Its database row stays, and addChunkLoader overwrites it.
        ChunkLoader existingChunkLoader = chunkLoaders.get(blockPosition);
        if (existingChunkLoader != null)
            removeChunkLoader((WChunkLoader) existingChunkLoader, RemoveReason.UNLOAD);

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
            if (!removeChunkLoader((WChunkLoader) chunkLoader, RemoveReason.UNLOAD))
                continue;

            UnloadedChunkLoader unloadedChunkLoader = new UnloadedChunkLoader(chunkLoader.getLoaderData(),
                    chunkLoader.getWhoPlaced().getUniqueId(), BlockPosition.of(chunkLoader.getLocation()),
                    chunkLoader.getTimeLeft());
            unloadedChunkLoaders.add(unloadedChunkLoader);

            updateTransaction = plugin.getDataHandler().saveLoaderTimeLeft(chunkLoader, updateTransaction);
        }

        if (updateTransaction != null)
            DBSession.execute(updateTransaction);

        this.unloadedChunkLoadersByWorlds.put(world.getName(), unloadedChunkLoaders);
    }

    @Override
    public void removeChunkLoader(ChunkLoader chunkLoader) {
        removeChunkLoader((WChunkLoader) chunkLoader, LoadersHandler.RemoveReason.DELETE);
    }

    public boolean removeChunkLoader(WChunkLoader chunkLoader, RemoveReason reason) {
        // The only guard: repeated and late calls stop here.
        if (chunkLoader.isRemoved())
            return false;

        // Its time ran out, or it is removed while its chunks are still loading (isNotActive() is true then).
        boolean expired = !chunkLoader.isInfinite() && chunkLoader.getTimeLeft() <= 0;
        boolean spawnParticle = reason == RemoveReason.DELETE && (expired || chunkLoader.isNotActive());

        // Holograms, tile entity and forced chunks. It releases getLoadedChunksCollection(): the forced
        // chunks when LOADED, nothing while LOADING. markRemoved() empties it, so it must come after.
        plugin.getNMSAdapter().removeLoader(chunkLoader, spawnParticle, SpawnerChangeListener.CALLBACK);
        chunkLoader.markRemoved();

        BlockPosition blockPosition = BlockPosition.of(chunkLoader.getLocation());
        chunkLoaders.remove(blockPosition, chunkLoader);
        for (ChunkPosition chunkPosition : chunkLoader.getChunkPositions()) {
            chunkLoadersByChunks.remove(chunkPosition, chunkLoader);
        }
        List<ChunkLoader> worldChunkLoaders = chunkLoadersByWorlds.get(blockPosition.getWorldName());
        if (worldChunkLoaders != null)
            worldChunkLoaders.remove(chunkLoader);

        if (reason != RemoveReason.SHUTDOWN)
            chunkLoader.getNPC().ifPresent(npc -> plugin.getNPCs().killNPC(npc));

        if (reason == RemoveReason.DELETE) {
            plugin.getDataHandler().deleteChunkLoader(chunkLoader);
            chunkLoader.getLocation().getBlock().setType(Material.AIR);
        }

        return true;
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
        for (ChunkLoader chunkLoader : chunkLoaders.values()) {
            removeChunkLoader((WChunkLoader) chunkLoader, RemoveReason.SHUTDOWN);
        }
    }

    public enum RemoveReason {

        /**
         * Broken, expired or removed through the API: its database row is deleted and its block cleared.
         */
        DELETE,

        /**
         * Its world unloads, or another loader replaces it: its database row and block stay.
         */
        UNLOAD,

        /**
         * The plugin disables: its database row, block and NPC stay.
         */
        SHUTDOWN

    }

}
