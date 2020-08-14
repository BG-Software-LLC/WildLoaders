package com.bgsoftware.wildloaders.loaders;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.UUID;

public final class WChunkLoader implements ChunkLoader {

    private static final WildLoadersPlugin plugin = WildLoadersPlugin.getPlugin();

    private final UUID whoPlaced;
    private final Location location;
    private final String loaderName;

    private boolean active = true;
    private long timeLeft;

    public WChunkLoader(String loaderName, Player whoPlaced, Location location, long timeLeft){
        this.loaderName = loaderName;
        this.whoPlaced = whoPlaced.getUniqueId();
        this.location = location.clone();
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
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public Optional<ChunkLoaderNPC> getNPC() {
        return plugin.getNPCs().getNPC(location);
    }

    @Override
    public void remove() {
        if(!Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTask(plugin, this::remove);
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

}
