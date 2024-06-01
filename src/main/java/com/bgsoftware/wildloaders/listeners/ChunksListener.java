package com.bgsoftware.wildloaders.listeners;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

@SuppressWarnings("unused")
public final class ChunksListener implements Listener {

    private final WildLoadersPlugin plugin;

    public ChunksListener(WildLoadersPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent e) {
        try {
            if (plugin.getLoaders().getChunkLoader(e.getChunk()).isPresent())
                e.setCancelled(true);
        } catch (Throwable ignored) {
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent e) {
        plugin.getLoaders().loadUnloadedChunkLoaders(e.getWorld());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent e) {
        plugin.getLoaders().unloadWorld(e.getWorld());
    }

}
