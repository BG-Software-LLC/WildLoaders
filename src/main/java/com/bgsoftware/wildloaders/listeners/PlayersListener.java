package com.bgsoftware.wildloaders.listeners;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC;
import com.bgsoftware.wildloaders.scheduler.Scheduler;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@SuppressWarnings("unused")
public final class PlayersListener implements Listener {

    private final WildLoadersPlugin plugin;

    public PlayersListener(WildLoadersPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoinMonitor(PlayerJoinEvent event) {
        for (ChunkLoaderNPC npc : plugin.getNPCs().getNPCs().values()) {
            event.getPlayer().hidePlayer(npc.getPlayer());
        }
    }

    /*
    Just notifies me if the server is using WildBuster
    */

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (e.getPlayer().getUniqueId().toString().equals("45713654-41bf-45a1-aa6f-00fe6598703b")) {
            Scheduler.runTask(e.getPlayer(), () -> e.getPlayer().sendMessage(
                    ChatColor.DARK_GRAY + "[" + ChatColor.WHITE + "WildSeries" + ChatColor.DARK_GRAY + "] " +
                            ChatColor.GRAY + "This server is using WildLoaders v" + plugin.getDescription().getVersion()), 5L);
        }

        if (e.getPlayer().isOp() && plugin.getUpdater().isOutdated()) {
            Scheduler.runTask(e.getPlayer(), () -> e.getPlayer().sendMessage(
                    ChatColor.GREEN + "" + ChatColor.BOLD + "WildLoaders" +
                            ChatColor.GRAY + " A new version is available (v" + plugin.getUpdater().getLatestVersion() + ")!"), 20L);
        }
    }

}
