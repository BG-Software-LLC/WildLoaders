package com.bgsoftware.wildloaders.utils.locations;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public final class LocationUtils {

    private LocationUtils(){}

    public static String getLocation(Location location){
        try {
            return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
        }catch (Exception ex){
            System.out.println(location);
            throw ex;
        }
    }

    public static Location getLocation(String location){
        String[] locationSections = location.split(",");

        if(locationSections.length != 4)
            throw new IllegalArgumentException("Cannot parse location " + location);

        String worldName = locationSections[0];
        double x = parseDouble(locationSections[1]);
        double y = parseDouble(locationSections[2]);
        double z = parseDouble(locationSections[3]);

        return new Location(Bukkit.getWorld(worldName), x, y, z);
    }

    private static double parseDouble(String str){
        try{
            return Double.parseDouble(str);
        }catch (Exception ex){
            throw new IllegalArgumentException("Cannot parse double " + str);
        }
    }

}
