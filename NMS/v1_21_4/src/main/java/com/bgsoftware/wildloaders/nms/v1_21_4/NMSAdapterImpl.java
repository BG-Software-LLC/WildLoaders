package com.bgsoftware.wildloaders.nms.v1_21_4;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Optional;

public class NMSAdapterImpl extends com.bgsoftware.wildloaders.nms.v1_21_4.AbstractNMSAdapter {

    @Override
    protected String getTagInternal(ItemStack itemStack, String key, String def) {
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag compoundTag = customData.getUnsafe();
            if (compoundTag.contains(key, 8))
                return compoundTag.getString(key);
        }
        return def;
    }

    @Override
    protected void setTagInternal(ItemStack itemStack, String key, String value) {
        CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        customData = customData.update(compoundTag -> compoundTag.putString(key, value));
        itemStack.set(DataComponents.CUSTOM_DATA, customData);
    }

    @Override
    protected long getTagInternal(ItemStack itemStack, String key, long def) {
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag compoundTag = customData.getUnsafe();
            if (compoundTag.contains(key, 4))
                return compoundTag.getLong(key);
        }
        return def;
    }

    @Override
    protected void setTagInternal(ItemStack itemStack, String key, long value) {
        CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        customData = customData.update(compoundTag -> compoundTag.putLong(key, value));
        itemStack.set(DataComponents.CUSTOM_DATA, customData);
    }

    @Override
    protected void setTextureForItem(ItemStack itemStack, String texture) {
        PropertyMap propertyMap = new PropertyMap();
        propertyMap.put("textures", new Property("textures", texture));

        ResolvableProfile resolvableProfile = new ResolvableProfile(Optional.empty(), Optional.empty(), propertyMap);

        itemStack.set(DataComponents.PROFILE, resolvableProfile);
    }

}
