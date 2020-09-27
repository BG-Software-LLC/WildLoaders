package com.bgsoftware.wildloaders.loaders;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import org.bukkit.inventory.ItemStack;

public final class WLoaderData implements LoaderData {

    private static final WildLoadersPlugin plugin = WildLoadersPlugin.getPlugin();

    private final String name;
    private final long timeLeft;
    private final ItemStack loaderItem;

    private int chunksRadius;
    private boolean chunksSpread;

    public WLoaderData(String name, long timeLeft, ItemStack loaderItem){
        this.name = name;
        this.timeLeft = timeLeft;
        this.loaderItem = plugin.getNMSAdapter().setTag(loaderItem, "loader-name", name);
        this.chunksRadius = 0;
        this.chunksSpread = false;
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

    @Override
    public void setChunksRadius(int chunksRadius) {
        this.chunksRadius = chunksRadius;
    }

    @Override
    public int getChunksRadius() {
        return chunksRadius;
    }

    @Override
    public void setChunksSpread(boolean chunksSpread) {
        this.chunksSpread = chunksSpread;
    }

    @Override
    public boolean isChunksSpread() {
        return chunksSpread;
    }

}
