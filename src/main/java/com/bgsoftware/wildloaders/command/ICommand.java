package com.bgsoftware.wildloaders.command;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface ICommand {

    String getLabel();

    String getUsage();

    String getPermission();

    String getDescription();

    int getMinArgs();

    int getMaxArgs();

    void perform(WildLoadersPlugin plugin, CommandSender sender, String[] args);

    List<String> tabComplete(WildLoadersPlugin plugin, CommandSender sender, String[] args);

}
