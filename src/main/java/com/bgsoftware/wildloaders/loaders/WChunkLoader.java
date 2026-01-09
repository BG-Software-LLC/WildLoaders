package com.bgsoftware.wildloaders.loaders;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.holograms.Hologram;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC;
import com.bgsoftware.wildloaders.scheduler.Scheduler;
import com.bgsoftware.wildloaders.utils.BlockPosition;
import com.bgsoftware.wildloaders.utils.ChunkLoaderChunks;
import com.bgsoftware.wildloaders.utils.SpawnerChangeListener;
import com.bgsoftware.wildloaders.utils.chunks.ChunkPosition;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class WChunkLoader implements ChunkLoader {

    private static final WildLoadersPlugin plugin = WildLoadersPlugin.getPlugin();

    private final List<Chunk> loadedChunksUnmodifiable;

    private final UUID whoPlaced;
    private final BlockPosition blockPosition;
    private final String loaderName;

    @Nullable
    private ITileEntityChunkLoader tileEntityChunkLoader;

    private boolean active = true;
    private boolean removed = true;

    private long timeLeft;

    public WChunkLoader(LoaderData loaderData, UUID whoPlaced, BlockPosition blockPosition, List<ChunkPosition> chunksToLoad, long timeLeft) {
        this.loaderName = loaderData.getName();
        this.whoPlaced = whoPlaced;
        this.blockPosition = blockPosition;
        this.timeLeft = timeLeft;
        List<Chunk> loadedChunks = new LinkedList<>();
        this.loadedChunksUnmodifiable = Collections.unmodifiableList(loadedChunks);
        ChunkLoaderChunks.loadChunksAsync(this, chunksToLoad, loadedChunks::add, this::onChunksLoadFinish);
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
        return this.removed || !this.active;
    }

    @Override
    public long getTimeLeft() {
        return timeLeft;
    }

    public void tick() {
        plugin.getProviders().tick(this.loadedChunksUnmodifiable);

        if (!isInfinite()) {
            timeLeft--;
            if (timeLeft < 0) {
                remove();
            } else if (timeLeft > 0 && timeLeft % 10 == 0) {
                plugin.getDataHandler().saveLoaderTimeLeft(this);
            }
        }
    }

    public boolean isInfinite() {
        return timeLeft == Integer.MIN_VALUE;
    }

    @Override
    public Location getLocation() {
        return this.blockPosition.getLocation();
    }

    @Override
    public Collection<Chunk> getLoadedChunksCollection() {
        return this.removed ? Collections.emptyList() : this.loadedChunksUnmodifiable;
    }

    @Override
    public Optional<ChunkLoaderNPC> getNPC() {
        return plugin.getNPCs().getNPC(this.blockPosition);
    }

    @Override
    public void remove() {
        Scheduler.ensureMain(getLocation(), this::removeInternal);
    }

    private void removeInternal() {
        plugin.getNMSAdapter().removeLoader(this, timeLeft <= 0 || isNotActive(),
                SpawnerChangeListener.CALLBACK);
        plugin.getLoaders().removeChunkLoader(this);

        getLocation().getBlock().setType(Material.AIR);
    }

    public void markRemoved() {
        this.removed = true;
        this.active = false;
    }

    @Override
    public ItemStack getLoaderItem() {
        return getLoaderData().getLoaderItem(getTimeLeft());
    }

    @Override
    public Collection<Hologram> getHolograms() {
        return this.tileEntityChunkLoader == null ? Collections.emptyList() : this.tileEntityChunkLoader.getHolograms();
    }

    @Nullable
    public ITileEntityChunkLoader getTileEntityChunkLoader() {
        return tileEntityChunkLoader;
    }

    public void forEachHologramLine(HologramLineCallback callback) {
        List<String> hologramLines = isInfinite() ?
                plugin.getSettings().infiniteHologramLines : plugin.getSettings().hologramLines;

        int index = hologramLines.size() - 1;
        for (String hologramLine : hologramLines) {
            callback.apply(index, hologramLine);
            --index;
        }
    }

    private void onChunksLoadFinish() {
        this.removed = false;
        this.tileEntityChunkLoader = plugin.getNMSAdapter().createLoader(this, SpawnerChangeListener.CALLBACK);
    }

    public interface HologramLineCallback {

        void apply(int index, String line);

    }


}
