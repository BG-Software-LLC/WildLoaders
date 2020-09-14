package com.bgsoftware.wildloaders.command;

import com.bgsoftware.wildloaders.Locale;
import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.command.commands.CmdGive;
import com.bgsoftware.wildloaders.command.commands.CmdList;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public final class CommandsHandler implements CommandExecutor, TabCompleter {

    private final List<ICommand> subCommands = new ArrayList<>();
    private final WildLoadersPlugin plugin;

    public CommandsHandler(WildLoadersPlugin plugin){
        this.plugin = plugin;
        subCommands.add(new CmdGive());
        subCommands.add(new CmdList());
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if(args.length > 0){
            for(ICommand subCommand : subCommands) {
                if (subCommand.getLabel().equalsIgnoreCase(args[0])){
                    if(subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())){
                        Locale.NO_PERMISSION.send(sender);
                        return false;
                    }
                    if(args.length < subCommand.getMinArgs() || args.length > subCommand.getMaxArgs()){
                        Locale.COMMAND_USAGE.send(sender, subCommand.getUsage());
                        return false;
                    }
                    subCommand.perform(plugin, sender, args);
                    return true;
                }
            }
        }


        Locale.HELP_COMMAND_HEADER.send(sender);
        for(ICommand subCommand : subCommands)
            if(subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission()))
                Locale.HELP_COMMAND_LINE.send(sender, subCommand.getUsage(), subCommand.getDescription());
        Locale.HELP_COMMAND_FOOTER.send(sender);

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if(args.length > 0){
            for(ICommand subCommand : subCommands) {
                if (subCommand.getLabel().equalsIgnoreCase(args[0])){
                    if(subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())){
                        return new ArrayList<>();
                    }
                    return subCommand.tabComplete(plugin, sender, args);
                }
            }
        }

        List<String> list = new ArrayList<>();

        for(ICommand subCommand : subCommands)
            if(subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission()))
                if(subCommand.getLabel().startsWith(args[0]))
                    list.add(subCommand.getLabel());

        return list;
    }
}
