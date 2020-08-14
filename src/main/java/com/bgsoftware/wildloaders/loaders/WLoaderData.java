package com.bgsoftware.wildloaders.loaders;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import org.bukkit.inventory.ItemStack;

public final class WLoaderData implements LoaderData {

    private static final WildLoadersPlugin plugin = WildLoadersPlugin.getPlugin();

    private final String name;
    private final long timeLeft;
    private final ItemStack loaderItem;

    public WLoaderData(String name, long timeLeft, ItemStack loaderItem){
        this.name = name;
        this.timeLeft = timeLeft;
        this.loaderItem = plugin.getNMSAdapter().setTag(loaderItem, "loader-name", name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getTimeLeft() {
        return timeLeft;
    }

    @Override
    public ItemStack getLoaderItem() {
        return loaderItem.clone();
    }
}
