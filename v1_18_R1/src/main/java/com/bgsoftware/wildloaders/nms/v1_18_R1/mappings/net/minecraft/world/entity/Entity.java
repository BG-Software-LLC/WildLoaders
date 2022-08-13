package com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.net.minecraft.world.entity;

import com.bgsoftware.common.remaps.Remap;
import com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.MappedObject;
import com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.net.minecraft.world.level.World;

public class Entity extends MappedObject<net.minecraft.world.entity.Entity> {

    public Entity(net.minecraft.world.entity.Entity handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "getLevel",
            type = Remap.Type.METHOD,
            remappedName = "W")
    public World getWorld() {
        return new World(handle.W());
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "moveTo",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public void moveTo(double x, double y, double z, float yaw, float pitch) {
        handle.b(x, y, z, yaw, pitch);
    }

}
