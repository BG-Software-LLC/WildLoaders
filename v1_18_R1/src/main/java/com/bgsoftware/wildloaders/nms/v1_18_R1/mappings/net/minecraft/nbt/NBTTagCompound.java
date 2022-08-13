package com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.net.minecraft.nbt;

import com.bgsoftware.common.remaps.Remap;
import com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.MappedObject;
import net.minecraft.nbt.NBTBase;

public class NBTTagCompound extends MappedObject<net.minecraft.nbt.NBTTagCompound> {

    public static NBTTagCompound ofNullable(net.minecraft.nbt.NBTTagCompound handle) {
        return handle == null ? null : new NBTTagCompound(handle);
    }

    public NBTTagCompound() {
        this(new net.minecraft.nbt.NBTTagCompound());
    }

    public NBTTagCompound(net.minecraft.nbt.NBTTagCompound handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "contains",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public boolean contains(String key, int type) {
        return handle.b(key, type);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "getString",
            type = Remap.Type.METHOD,
            remappedName = "l")
    public String getString(String key) {
        return handle.l(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "put",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void put(String key, NBTBase nbtBase) {
        handle.a(key, nbtBase);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "getLong",
            type = Remap.Type.METHOD,
            remappedName = "i")
    public long getLong(String key) {
        return handle.i(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "contains",
            type = Remap.Type.METHOD,
            remappedName = "e")
    public boolean contains(String key) {
        return handle.e(key);
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "getCompound",
            type = Remap.Type.METHOD,
            remappedName = "p")
    public NBTTagCompound getCompound(String key) {
        return new NBTTagCompound(handle.p(key));
    }

    @Remap(classPath = "net.minecraft.nbt.CompoundTag",
            name = "putString",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void putString(String key, String value) {
        handle.a(key, value);
    }

}
