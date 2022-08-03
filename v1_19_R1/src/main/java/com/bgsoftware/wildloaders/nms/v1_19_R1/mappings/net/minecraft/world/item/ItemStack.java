package com.bgsoftware.wildloaders.nms.v1_19_R1.mappings.net.minecraft.world.item;

import com.bgsoftware.wildloaders.nms.mapping.Remap;
import com.bgsoftware.wildloaders.nms.v1_19_R1.mappings.MappedObject;
import com.bgsoftware.wildloaders.nms.v1_19_R1.mappings.net.minecraft.nbt.NBTTagCompound;

public class ItemStack extends MappedObject<net.minecraft.world.item.ItemStack> {

    public ItemStack(net.minecraft.world.item.ItemStack handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "getOrCreateTag",
            type = Remap.Type.METHOD,
            remappedName = "v")
    public NBTTagCompound getOrCreateTag() {
        return new NBTTagCompound(handle.v());
    }

    @Remap(classPath = "net.minecraft.world.item.ItemStack",
            name = "getTag",
            type = Remap.Type.METHOD,
            remappedName = "u")
    public NBTTagCompound getTag() {
        return NBTTagCompound.ofNullable(handle.u());
    }

}
