package com.bgsoftware.wildloaders.command.commands;

import com.bgsoftware.wildloaders.Locale;
import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.command.ICommand;
import com.bgsoftware.wildloaders.utils.TimeUtils;
import com.bgsoftware.wildloaders.utils.items.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CmdGive implements ICommand {

    @Override
    public String getLabel() {
        return "give";
    }

    @Override
    public String getUsage() {
        return "loader give <player-name> <loader-name> [amount] [time]";
    }

    @Override
    public String getPermission() {
        return "wildloaders.give";
    }

    @Override
    public String getDescription() {
        return "Give a chunk-loader item to a specific player.";
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 5;
    }

    @Override
    public void perform(WildLoadersPlugin plugin, CommandSender sender, String[] args) {
        Player target = Bukkit.getPlayer(args[1]);

        if(target == null){
            Locale.INVALID_PLAYER.send(sender, args[1]);
            return;
        }

        Optional<LoaderData> optionalLoaderData = plugin.getLoaders().getLoaderData(args[2]);

        if(!optionalLoaderData.isPresent()){
            Locale.INVALID_LOADER.send(sender, args[2]);
            return;
        }

        if(!target.equals(sender) && !sender.hasPermission(getPermission() + ".other")){
            Locale.NO_PERMISSION.send(sender);
            return;
        }

        if(target.equals(sender) && !(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "You must give a chunk-loader item to a valid player.");
            return;
        }

        int amount = 1;

        if(args.length == 4){
            try{
                amount = Integer.parseInt(args[3]);
            } catch (IllegalArgumentException e){
                Locale.INVALID_AMOUNT.send(sender, args[3]);
                return;
            }
        }

        LoaderData loaderData = optionalLoaderData.get();

        ItemStack itemStack = args.length == 5 ? loaderData.getLoaderItem(TimeUtils.fromString(args[4])) : loaderData.getLoaderItem();
        itemStack.setAmount(amount);

        ItemUtils.addItems(target.getInventory(), target.getLocation(), itemStack);

        Locale.GIVE_SUCCESS.send(sender, amount, loaderData.getName(), target.getName());
        Locale.RECEIVE_SUCCESS.send(target, amount, loaderData.getName(), sender.getName());
    }

    @Override
    public List<String> tabComplete(WildLoadersPlugin plugin, CommandSender sender, String[] args) {
        if(!sender.hasPermission(getPermission()))
            return new ArrayList<>();

        if (args.length == 3) {
            List<String> list = new ArrayList<>();
            for(LoaderData loaderData : plugin.getLoaders().getLoaderDatas())
                if(loaderData.getName().startsWith(args[2]))
                    list.add(loaderData.getName());
            return list;
        }

        if (args.length >= 4) {
            return new ArrayList<>();
        }

        return null;
    }
}
