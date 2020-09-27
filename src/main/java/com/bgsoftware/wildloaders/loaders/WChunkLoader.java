package com.bgsoftware.wildloaders.loaders;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class WChunkLoader implements ChunkLoader {

    private static final WildLoadersPlugin plugin = WildLoadersPlugin.getPlugin();

    private final UUID whoPlaced;
    private final Location location;
    private final Chunk[] loadedChunks;
    private final String loaderName;

    private boolean active = true;
    private long timeLeft;

    public WChunkLoader(LoaderData loaderData, UUID whoPlaced, Location location, long timeLeft){
        this.loaderName = loaderData.getName();
        this.whoPlaced = whoPlaced;
        this.location = location.clone();
        this.loadedChunks = calculateChunks(loaderData, this.location);
        this.timeLeft = timeLeft;
        plugin.getNMSAdapter().createLoader(this);
    }

    @Override
    public LoaderData getLoaderData() {
        return plugin.getLoaders().getLoaderData(loaderName).orElse(null);
    }

    @Override
    public OfflinePlayer getWhoPlaced() {
        return Bukkit.getOfflinePlayer(whoPlaced);
    }

    public boolean isNotActive(){
        if(active)
            active = plugin.getLoaders().getChunkLoader(getLocation()).orElse(null) == this;
        return !active;
    }

    @Override
    public long getTimeLeft() {
        return timeLeft;
    }

    public void tick(){
        timeLeft--;
        if(timeLeft < 0) {
            remove();
        }
        else if(timeLeft > 0 && timeLeft % 10 == 0){
            Query.UPDATE_CHUNK_LOADER_TIME_LEFT.insertParameters()
                    .setObject(timeLeft)
                    .setLocation(location)
                    .queue(location);
        }
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
        if(!Bukkit.isPrimaryThread()){
            Executor.sync(this::remove);
            return;
        }

        plugin.getNMSAdapter().removeLoader(this, timeLeft <= 0 || isNotActive());
        plugin.getLoaders().removeChunkLoader(this);

        getLocation().getBlock().setType(Material.AIR);
    }

    @Override
    public ItemStack getLoaderItem() {
        ItemStack itemStack = getLoaderData().getLoaderItem();
        return plugin.getNMSAdapter().setTag(itemStack, "loader-time", getTimeLeft());
    }

    private static Chunk[] calculateChunks(LoaderData loaderData, Location original){
        List<Chunk> chunkList = new ArrayList<>();

        int chunkX = original.getBlockX() >> 4, chunkZ = original.getBlockZ() >> 4;

        for(int x = -loaderData.getChunksRadius(); x <= loaderData.getChunksRadius(); x++)
            for(int z = -loaderData.getChunksRadius(); z <= loaderData.getChunksRadius(); z++)
                chunkList.add(original.getWorld().getChunkAt(chunkX + x, chunkZ + z));

        return chunkList.toArray(new Chunk[0]);
    }

}
