package com.bgsoftware.wildloaders.loaders;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.utils.TimeUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

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
    public boolean isInfinite() {
        return timeLeft == Integer.MIN_VALUE;
    }

    @Override
    public ItemStack getLoaderItem() {
        return getLoaderItem(getTimeLeft());
    }

    @Override
    public ItemStack getLoaderItem(long timeLeft) {
        ItemStack itemStack = loaderItem.clone();

        ItemMeta itemMeta = itemStack.getItemMeta();

        if(itemMeta != null){
            String formattedTime = isInfinite() ? "" : TimeUtils.formatTime(timeLeft);

            if(itemMeta.hasDisplayName()) {
                itemMeta.setDisplayName(itemMeta.getDisplayName().replace("{}", formattedTime));
            }

            if(itemMeta.hasLore()){
                List<String> lore = new ArrayList<>(itemMeta.getLore().size());

                for(String line : itemMeta.getLore())
                    lore.add(line.replace("{}", formattedTime));

                itemMeta.setLore(lore);
            }

            itemStack.setItemMeta(itemMeta);
        }

        return plugin.getNMSAdapter().setTag(itemStack, "loader-time", timeLeft);
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
