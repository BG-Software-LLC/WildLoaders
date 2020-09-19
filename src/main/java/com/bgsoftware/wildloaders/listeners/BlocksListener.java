package com.bgsoftware.wildloaders.listeners;

import com.bgsoftware.wildloaders.Locale;
import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.utils.chunks.ChunkPosition;
import com.bgsoftware.wildloaders.utils.legacy.Materials;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

@SuppressWarnings("unused")
public final class BlocksListener implements Listener {

    private final WildLoadersPlugin plugin;

    public BlocksListener(WildLoadersPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLoaderPlace(BlockPlaceEvent e){
        String loaderName = plugin.getNMSAdapter().getTag(e.getItemInHand(), "loader-name", "");
        Optional<LoaderData> optionalLoaderData = plugin.getLoaders().getLoaderData(loaderName);

        if(!optionalLoaderData.isPresent())
            return;

        if(!e.getPlayer().hasPermission("wildloaders.use")) {
            e.setCancelled(true);
            Locale.NO_PLACE_PERMISSION.send(e.getPlayer());
            return;
        }

        if(plugin.getLoaders().getChunkLoader(e.getBlock().getLocation().getChunk()).isPresent()){
            e.setCancelled(true);
            Locale.ALREADY_LOADED.send(e.getPlayer());
            return;
        }

        LoaderData loaderData = optionalLoaderData.get();

        long timeLeft = plugin.getNMSAdapter().getTag(e.getItemInHand(), "loader-time", loaderData.getTimeLeft());

        plugin.getLoaders().addChunkLoader(loaderData, e.getPlayer(), e.getBlock().getLocation(), timeLeft);

        Locale.PLACED_LOADER.send(e.getPlayer(), ChunkPosition.of(e.getBlock().getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLoaderBreak(BlockBreakEvent e){
        Location blockLoc = e.getBlock().getLocation();
        Optional<ChunkLoader> optionalChunkLoader = plugin.getLoaders().getChunkLoader(blockLoc);

        if(!optionalChunkLoader.isPresent())
            return;

        ChunkLoader chunkLoader = optionalChunkLoader.get();
        chunkLoader.remove();

        if(e.getPlayer().getGameMode() != GameMode.CREATIVE)
            blockLoc.getWorld().dropItemNaturally(blockLoc, chunkLoader.getLoaderItem());

        Locale.BROKE_LOADER.send(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawnerPlace(BlockPlaceEvent e){
        if(e.getBlock().getType() != Materials.SPAWNER.toBukkitType())
            return;

        if(!plugin.getLoaders().getChunkLoader(e.getBlock().getChunk()).isPresent())
            return;

        plugin.getNMSAdapter().updateSpawner(e.getBlock().getLocation(), false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLoaderInteract(PlayerInteractEvent e){
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if(plugin.getLoaders().getChunkLoader(e.getClickedBlock().getLocation()).isPresent())
            e.setCancelled(true);
    }

}
