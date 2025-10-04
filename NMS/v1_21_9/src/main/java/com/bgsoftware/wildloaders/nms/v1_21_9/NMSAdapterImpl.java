package com.bgsoftware.wildloaders.nms.v1_21_9;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Optional;

public class NMSAdapterImpl extends com.bgsoftware.wildloaders.nms.v1_21_9.AbstractNMSAdapter {

    @Override
    protected String getTagInternal(ItemStack itemStack, String key, String def) {
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag compoundTag = customData.getUnsafe();
            return compoundTag.getStringOr(key, def);
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
            return compoundTag.getLongOr(key, def);
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
        Multimap<String, Property> properties = HashMultimap.create();
        properties.put("textures", new Property("textures", texture));

        ResolvableProfile.Partial partialProfile = new ResolvableProfile.Partial(
                Optional.empty(), Optional.empty(), new PropertyMap(properties));
        ResolvableProfile resolvableProfile = new ResolvableProfile.Static(Either.right(partialProfile), PlayerSkin.Patch.EMPTY);

        itemStack.set(DataComponents.PROFILE, resolvableProfile);
    }

}
