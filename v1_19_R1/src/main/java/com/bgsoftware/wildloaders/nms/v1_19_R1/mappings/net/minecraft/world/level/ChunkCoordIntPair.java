package com.bgsoftware.wildloaders.nms.v1_19_R1.mappings.net.minecraft.world.level;

import com.bgsoftware.wildloaders.nms.mapping.Remap;
import com.bgsoftware.wildloaders.nms.v1_19_R1.mappings.MappedObject;

public class ChunkCoordIntPair extends MappedObject<net.minecraft.world.level.ChunkCoordIntPair> {

    public ChunkCoordIntPair(net.minecraft.world.level.ChunkCoordIntPair handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.ChunkPos",
            name = "x",
            type = Remap.Type.FIELD,
            remappedName = "e")
    public int getX() {
        return handle.e;
    }

    @Remap(classPath = "net.minecraft.world.level.ChunkPos",
            name = "z",
            type = Remap.Type.FIELD,
            remappedName = "f")
    public int getZ() {
        return handle.f;
    }

    @Remap(classPath = "net.minecraft.world.level.ChunkPos",
            name = "asLong",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public static long asLong(int x, int z) {
        return net.minecraft.world.level.ChunkCoordIntPair.c(x, z);
    }

}
