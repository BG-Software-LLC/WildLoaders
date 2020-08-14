package com.bgsoftware.wildloaders.listeners;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class PlayersListener implements Listener {

    private WildLoadersPlugin plugin;

    public PlayersListener(WildLoadersPlugin plugin){
        this.plugin = plugin;
    }

    /**
     * In some versions, the loaders can die even if they are in creative.
     * This should fix the issue.
     */

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLoaderDamage(EntityDamageEvent e){
        if(!(e.getEntity() instanceof LivingEntity))
            return;

        if(plugin.getNPCs().isNPC((LivingEntity) e.getEntity())) {
            e.setCancelled(true);
            e.setDamage(0D);
        }
    }

}
