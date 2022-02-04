package com.bgsoftware.wildloaders.loaders;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.holograms.Hologram;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC;
import com.bgsoftware.wildloaders.utils.database.Query;
import com.bgsoftware.wildloaders.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class WChunkLoader implements ChunkLoader {

    private static final WildLoadersPlugin plugin = WildLoadersPlugin.getPlugin();

    private final UUID whoPlaced;
    private final Location location;
    private final Chunk[] loadedChunks;
    private final String loaderName;
    private final ITileEntityChunkLoader tileEntityChunkLoader;

    private boolean active = true;
    private long timeLeft;

    public WChunkLoader(LoaderData loaderData, UUID whoPlaced, Location location, long timeLeft) {
        this.loaderName = loaderData.getName();
        this.whoPlaced = whoPlaced;
        this.location = location.clone();
        this.loadedChunks = calculateChunks(loaderData, whoPlaced, this.location);
        this.timeLeft = timeLeft;
        this.tileEntityChunkLoader = plugin.getNMSAdapter().createLoader(this);
    }

    @Override
    public LoaderData getLoaderData() {
        return plugin.getLoaders().getLoaderData(loaderName).orElse(null);
    }

    @Override
    public OfflinePlayer getWhoPlaced() {
        return Bukkit.getOfflinePlayer(whoPlaced);
    }

    public boolean isNotActive() {
        if (active)
            active = plugin.getLoaders().getChunkLoader(getLocation()).orElse(null) == this;
        return !active;
    }

    @Override
    public long getTimeLeft() {
        return timeLeft;
    }

    public void tick() {
        plugin.getProviders().tick(loadedChunks);

        if (!isInfinite()) {
            timeLeft--;
            if (timeLeft < 0) {
                remove();
            } else if (timeLeft > 0 && timeLeft % 10 == 0) {
                Query.UPDATE_CHUNK_LOADER_TIME_LEFT.insertParameters()
                        .setObject(timeLeft)
                        .setLocation(location)
                        .queue(location);
            }
        }
    }

    public boolean isInfinite() {
        return timeLeft == Integer.MIN_VALUE;
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public Chunk[] getLoadedChunks() {
        return loadedChunks;
    }

    @Override
    public Optional<ChunkLoaderNPC> getNPC() {
        return plugin.getNPCs().getNPC(location);
    }

    @Override
    public void remove() {
        if (!Bukkit.isPrimaryThread()) {
            Executor.sync(this::remove);
            return;
        }

        plugin.getNMSAdapter().removeLoader(this, timeLeft <= 0 || isNotActive());
        plugin.getLoaders().removeChunkLoader(this);

        getLocation().getBlock().setType(Material.AIR);
    }

    @Override
    public ItemStack getLoaderItem() {
        return getLoaderData().getLoaderItem(getTimeLeft());
    }

    @Override
    public Collection<Hologram> getHolograms() {
        return tileEntityChunkLoader.getHolograms();
    }

    public List<String> getHologramLines() {
        return isInfinite() ? plugin.getSettings().infiniteHologramLines : plugin.getSettings().hologramLines;
    }

    private static Chunk[] calculateChunks(LoaderData loaderData, UUID whoPlaced, Location original) {
        List<Chunk> chunkList = new ArrayList<>();

        if (loaderData.isChunksSpread()) {
            calculateClaimChunks(original.getChunk(), whoPlaced, chunkList);
        }

        if (chunkList.isEmpty()) {
            int chunkX = original.getBlockX() >> 4, chunkZ = original.getBlockZ() >> 4;

            for (int x = -loaderData.getChunksRadius(); x <= loaderData.getChunksRadius(); x++)
                for (int z = -loaderData.getChunksRadius(); z <= loaderData.getChunksRadius(); z++)
                    chunkList.add(original.getWorld().getChunkAt(chunkX + x, chunkZ + z));
        }

        return chunkList.toArray(new Chunk[0]);
    }

    private static void calculateClaimChunks(Chunk originalChunk, UUID whoPlaced, List<Chunk> chunkList) {
        if (!plugin.getProviders().hasChunkAccess(whoPlaced, originalChunk))
            return;

        chunkList.add(originalChunk);

        int chunkX = originalChunk.getX(), chunkZ = originalChunk.getZ();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x != 0 || z != 0) // We don't want to add the originalChunk again.
                    calculateClaimChunks(originalChunk.getWorld().getChunkAt(chunkX + x, chunkZ + z), whoPlaced, chunkList);
            }
        }

    }

}
