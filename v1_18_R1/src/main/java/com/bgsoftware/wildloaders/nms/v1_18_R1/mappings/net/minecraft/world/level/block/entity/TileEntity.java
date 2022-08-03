package com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.net.minecraft.world.level.block.entity;

import com.bgsoftware.wildloaders.nms.mapping.Remap;
import com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.MappedObject;
import net.minecraft.world.level.MobSpawnerAbstract;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;

public class TileEntity extends MappedObject<net.minecraft.world.level.block.entity.TileEntity> {

    public TileEntity(net.minecraft.world.level.block.entity.TileEntity handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.block.entity.SpawnerBlockEntity",
            name = "getSpawner",
            type = Remap.Type.METHOD,
            remappedName = "d")
    public MobSpawnerAbstract getSpawner() {
        return ((TileEntityMobSpawner) handle).d();
    }

}
