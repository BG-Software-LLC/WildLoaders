package com.bgsoftware.wildloaders;

import com.bgsoftware.wildloaders.utils.config.CommentedConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public final class Locale {

    private static final WildLoadersPlugin plugin = WildLoadersPlugin.getPlugin();
    private static final Map<String, Locale> localeMap = new HashMap<>();

    public static Locale ALREADY_LOADED = new Locale("ALREADY_LOADED");
    public static Locale BROKE_LOADER = new Locale("BROKE_LOADER");
    public static Locale COMMAND_USAGE = new Locale("COMMAND_USAGE");
    public static Locale GIVE_SUCCESS = new Locale("GIVE_SUCCESS");
    public static Locale HELP_COMMAND_HEADER = new Locale("HELP_COMMAND_HEADER");
    public static Locale HELP_COMMAND_LINE = new Locale("HELP_COMMAND_LINE");
    public static Locale HELP_COMMAND_FOOTER = new Locale("HELP_COMMAND_FOOTER");
    public static Locale INVALID_AMOUNT = new Locale("INVALID_AMOUNT");
    public static Locale INVALID_LOADER = new Locale("INVALID_LOADER");
    public static Locale INVALID_PLAYER = new Locale("INVALID_PLAYER");
    public static Locale LIST_LOADERS_HEADER = new Locale("LIST_LOADERS_HEADER");
    public static Locale LIST_LOADERS_LINE = new Locale("LIST_LOADERS_LINE");
    public static Locale LIST_LOADERS_FOOTER = new Locale("LIST_LOADERS_FOOTER");
    public static Locale NO_PERMISSION = new Locale("NO_PERMISSION");
    public static Locale NO_PLACE_PERMISSION = new Locale("NO_PLACE_PERMISSION");
    public static Locale PLACED_LOADER = new Locale("PLACED_LOADER");
    public static Locale RECEIVE_SUCCESS = new Locale("RECEIVE_SUCCESS");
    public static Locale TIME_PLACEHOLDER_DAYS = new Locale("TIME_PLACEHOLDER_DAYS");
    public static Locale TIME_PLACEHOLDER_DAY = new Locale("TIME_PLACEHOLDER_DAY");
    public static Locale TIME_PLACEHOLDER_HOURS = new Locale("TIME_PLACEHOLDER_HOURS");
    public static Locale TIME_PLACEHOLDER_HOUR = new Locale("TIME_PLACEHOLDER_HOUR");
    public static Locale TIME_PLACEHOLDER_MINUTES = new Locale("TIME_PLACEHOLDER_MINUTES");
    public static Locale TIME_PLACEHOLDER_MINUTE = new Locale("TIME_PLACEHOLDER_MINUTE");
    public static Locale TIME_PLACEHOLDER_SECONDS = new Locale("TIME_PLACEHOLDER_SECONDS");
    public static Locale TIME_PLACEHOLDER_SECOND = new Locale("TIME_PLACEHOLDER_SECOND");

    private Locale(String identifier){
        localeMap.put(identifier, this);
    }

    private String message;

    public String getMessage(Object... objects){
        if(message != null && !message.equals("")) {
            String msg = message;

            for (int i = 0; i < objects.length; i++)
                msg = msg.replace("{" + i + "}", objects[i].toString());

            return msg;
        }

        return null;
    }

    public boolean isEmpty(){
        return message == null || message.equals("");
    }

    public void send(CommandSender sender, Object... objects){
        String message = getMessage(objects);
        if(message != null && sender != null)
            sender.sendMessage(message);
    }

    private void setMessage(String message){
        this.message = message;
    }

    public static void reload(){
        WildLoadersPlugin.log("Loading messages started...");
        long startTime = System.currentTimeMillis();
        int messagesAmount = 0;
        File file = new File(plugin.getDataFolder(), "lang.yml");

        if(!file.exists())
            plugin.saveResource("lang.yml", false);

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        cfg.syncWithConfig(file, plugin.getResource("lang.yml"));

        for(String identifier : localeMap.keySet()){
            localeMap.get(identifier).setMessage(ChatColor.translateAlternateColorCodes('&', cfg.getString(identifier, "")));
            messagesAmount++;
        }

        WildLoadersPlugin.log(" - Found " + messagesAmount + " messages in lang.yml.");
        WildLoadersPlugin.log("Loading messages done (Took " + (System.currentTimeMillis() - startTime) + "ms)");
    }

    public static void sendMessage(CommandSender sender, String message){
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

}
