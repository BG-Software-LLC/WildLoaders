package com.bgsoftware.wildloaders.nms.v1_18_R2.loader;

import com.bgsoftware.common.remaps.Remap;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;

public record TileEntityChunkLoaderTicker(TileEntityChunkLoader tileEntityChunkLoader) implements TickingBlockEntity {

    @Remap(classPath = "net.minecraft.world.level.block.entity.TickingBlockEntity",
            name = "tick",
            type = Remap.Type.METHOD,
            remappedName = "a")
    @Override
    public void a() {
        tileEntityChunkLoader.tick();
    }

    @Remap(classPath = "net.minecraft.world.level.block.entity.TickingBlockEntity",
            name = "isRemoved",
            type = Remap.Type.METHOD,
            remappedName = "b")
    @Override
    public boolean b() {
        return tileEntityChunkLoader.isRemoved();
    }

    @Remap(classPath = "net.minecraft.world.level.block.entity.TickingBlockEntity",
            name = "getPos",
            type = Remap.Type.METHOD,
            remappedName = "c")
    @Override
    public BlockPosition c() {
        return tileEntityChunkLoader.tilePosition.getHandle();
    }

    @Remap(classPath = "net.minecraft.world.level.block.entity.TickingBlockEntity",
            name = "getType",
            type = Remap.Type.METHOD,
            remappedName = "d")
    @Override
    public String d() {
        return TileEntityTypes.a(tileEntityChunkLoader.getType()) + "";
    }

}
