package com.bgsoftware.wildloaders.command.commands;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.command.ICommand;
import com.bgsoftware.wildloaders.utils.threads.Executor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public final class CmdSave implements ICommand {

    @Override
    public String getLabel() {
        return "save";
    }

    @Override
    public String getUsage() {
        return "loader save";
    }

    @Override
    public String getPermission() {
        return "wildloaders.save";
    }

    @Override
    public String getDescription() {
        return "Save all the chunk-loaders into the database.";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public void perform(WildLoadersPlugin plugin, CommandSender sender, String[] args) {
        Executor.data(() -> {
            long startTime = System.currentTimeMillis();
            sender.sendMessage(ChatColor.YELLOW + "Saving all chunk loaders...");
            plugin.getDataHandler().saveDatabase();
            sender.sendMessage(ChatColor.YELLOW + "Saving chunk loaders done! (Took " + (System.currentTimeMillis() - startTime) + "ms)");
        });
    }

    @Override
    public List<String> tabComplete(WildLoadersPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}
