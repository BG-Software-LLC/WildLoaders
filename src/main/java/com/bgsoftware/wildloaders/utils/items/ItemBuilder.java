package com.bgsoftware.wildloaders.utils.items;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public final class ItemBuilder {

    private static final WildLoadersPlugin plugin = WildLoadersPlugin.getPlugin();

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;
    private String texture = "";

    public ItemBuilder(Material type){
        this(type, 1);
    }

    public ItemBuilder(Material type, int amount){
        this(type, amount, (short) 0);
    }

    public ItemBuilder(Material type, short damage){
        this(type, 1, damage);
    }

    public ItemBuilder(Material type, int amount, short damage){
        this(new ItemStack(type, amount, damage));
    }

    public ItemBuilder(ItemStack itemStack){
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder setDisplayName(String name){
        itemMeta.setDisplayName(name);
        return this;
    }

    public ItemBuilder setLore(String... lore){
        return setLore(Arrays.asList(lore));
    }

    public ItemBuilder setLore(List<String> lore){
        itemMeta.setLore(lore);
        return this;
    }

    public ItemBuilder setOwner(String playerName){
        if(itemMeta instanceof SkullMeta){
            ((SkullMeta) itemMeta).setOwner(playerName);
        }
        return this;
    }

    public ItemBuilder setTexture(String texture){
        this.texture = texture;
        return this;
    }

    public ItemBuilder addEnchant(Enchantment ench, int level){
        itemMeta.addEnchant(ench, level, true);
        return this;
    }

    public ItemStack build(){
        itemStack.setItemMeta(itemMeta);
        return texture.isEmpty() ? itemStack : plugin.getNMSAdapter().getPlayerSkull(itemStack, texture);
    }

}
