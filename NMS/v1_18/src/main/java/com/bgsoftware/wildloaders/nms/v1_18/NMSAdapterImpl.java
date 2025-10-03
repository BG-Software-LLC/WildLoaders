package com.bgsoftware.wildloaders.nms.v1_18;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class NMSAdapterImpl extends com.bgsoftware.wildloaders.nms.v1_18.AbstractNMSAdapter {

    @Override
    protected String getTagInternal(ItemStack itemStack, String key, String def) {
        CompoundTag compoundTag = itemStack.getTag();
        return compoundTag == null || !compoundTag.contains(key, 8) ? def : compoundTag.getString(key);
    }

    @Override
    protected void setTagInternal(ItemStack itemStack, String key, String value) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        compoundTag.putString(key, value);
    }

    @Override
    protected long getTagInternal(ItemStack itemStack, String key, long def) {
        CompoundTag compoundTag = itemStack.getTag();
        return compoundTag == null || !compoundTag.contains(key, 4) ? def : compoundTag.getLong(key);
    }

    @Override
    protected void setTagInternal(ItemStack itemStack, String key, long value) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();
        compoundTag.putLong(key, value);
    }

    @Override
    protected void setTextureForItem(ItemStack itemStack, String texture) {
        CompoundTag compoundTag = itemStack.getOrCreateTag();

        CompoundTag skullOwner = compoundTag.contains("SkullOwner") ?
                compoundTag.getCompound("SkullOwner") : new CompoundTag();

        CompoundTag properties = new CompoundTag();
        ListTag textures = new ListTag();
        CompoundTag signature = new CompoundTag();

        signature.putString("Value", texture);
        textures.add(signature);

        properties.put("textures", textures);

        skullOwner.put("Properties", properties);
        skullOwner.putString("Id", UUID.randomUUID().toString());

        compoundTag.put("SkullOwner", skullOwner);
    }

}
