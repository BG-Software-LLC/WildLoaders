package com.bgsoftware.wildloaders.command.commands;

import com.bgsoftware.wildloaders.Locale;
import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.command.ICommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public final class CmdList implements ICommand {

    @Override
    public String getLabel() {
        return "list";
    }

    @Override
    public String getUsage() {
        return "loader list";
    }

    @Override
    public String getPermission() {
        return "wildloaders.list";
    }

    @Override
    public String getDescription() {
        return "Show all the available chunk loaders on the server.";
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
        StringBuilder message = new StringBuilder();

        if(!Locale.LIST_LOADERS_HEADER.isEmpty())
            message.append(Locale.LIST_LOADERS_HEADER.getMessage()).append("\n");

        if(!Locale.LIST_LOADERS_LINE.isEmpty()) {
            for (LoaderData loaderData : plugin.getLoaders().getLoaderDatas()) {
                message.append(Locale.LIST_LOADERS_LINE.getMessage(loaderData.getName(), loaderData.getTimeLeft())).append("\n");
            }
        }

        String parsedMessage;

        if(!Locale.LIST_LOADERS_FOOTER.isEmpty()) {
            message.append(Locale.LIST_LOADERS_FOOTER.getMessage());
            parsedMessage = message.toString();
        }
        else{
            parsedMessage = message.substring(0, message.length() - 1);
        }

        sender.sendMessage(parsedMessage);
    }

    @Override
    public List<String> tabComplete(WildLoadersPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
