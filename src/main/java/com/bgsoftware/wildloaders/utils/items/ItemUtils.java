package com.bgsoftware.wildloaders.utils.items;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public final class ItemUtils {

    private ItemUtils(){

    }

    public static void addItems(Inventory inventory, Location location, ItemStack... itemStacks){
        HashMap<Integer, ItemStack> leftOvers = inventory.addItem(itemStacks);
        if(!leftOvers.isEmpty() && location != null)
            leftOvers.values().forEach(itemStack -> location.getWorld().dropItemNaturally(location, itemStack));
    }

}
