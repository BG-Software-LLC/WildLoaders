package com.bgsoftware.wildloaders.listeners;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

@SuppressWarnings("unused")
public final class ChunksListener implements Listener {

    private final WildLoadersPlugin plugin;

    public ChunksListener(WildLoadersPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent e){
        try {
            if (plugin.getLoaders().getChunkLoader(e.getChunk()).isPresent())
                e.setCancelled(true);
        }catch (Throwable ignored){}
    }

}
