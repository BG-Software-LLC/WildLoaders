package com.bgsoftware.wildloaders.listeners;

import com.bgsoftware.wildloaders.Updater;
import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.utils.threads.Executor;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@SuppressWarnings("unused")
public final class PlayersListener implements Listener {

    private final WildLoadersPlugin plugin;

    public PlayersListener(WildLoadersPlugin plugin){
        this.plugin = plugin;
    }

    /*
    Just notifies me if the server is using WildBuster
    */

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        if(e.getPlayer().getUniqueId().toString().equals("45713654-41bf-45a1-aa6f-00fe6598703b")){
            Executor.sync(() -> e.getPlayer().sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.WHITE + "WildSeries" + ChatColor.DARK_GRAY + "] " +
                    ChatColor.GRAY + "This server is using WildLoaders v" + plugin.getDescription().getVersion()), 5L);
        }

        if(e.getPlayer().isOp() && Updater.isOutdated()){
            Executor.sync(() -> e.getPlayer().sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "WildLoaders" +
                    ChatColor.GRAY + " A new version is available (v" + Updater.getLatestVersion() + ")!"), 20L);
        }
    }

}
