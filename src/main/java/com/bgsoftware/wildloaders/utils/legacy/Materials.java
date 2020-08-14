package com.bgsoftware.wildloaders.utils.legacy;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum Materials {

    SPAWNER("MOB_SPAWNER");

    Materials(String bukkitType){
        this(bukkitType, 0);
    }

    Materials(String bukkitType, int bukkitData){
        this.bukkitType = bukkitType;
        this.bukkitData = (short) bukkitData;
    }

    private final String bukkitType;
    private final short bukkitData;

    public Material toBukkitType(){
        try {
            try {
                return Material.valueOf(bukkitType);
            } catch (IllegalArgumentException ex) {
                return Material.valueOf(name());
            }
        }catch(Exception ex){
            throw new IllegalArgumentException("Couldn't cast " + name() + " into a bukkit enum. Contact Ome_R!");
        }
    }

    public ItemStack toBukkitItem(){
        return toBukkitItem(1);
    }

    public ItemStack toBukkitItem(int amount){
        return bukkitData == 0 ? new ItemStack(toBukkitType(), amount) : new ItemStack(toBukkitType(), amount, bukkitData);
    }

}
