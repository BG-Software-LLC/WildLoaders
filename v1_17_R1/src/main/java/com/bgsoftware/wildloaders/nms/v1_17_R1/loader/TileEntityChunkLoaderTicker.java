package com.bgsoftware.wildloaders.nms.v1_17_R1.loader;

import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.entity.TileEntityTypes;

public record TileEntityChunkLoaderTicker(TileEntityChunkLoader tileEntityChunkLoader) implements TickingBlockEntity {

    @Override
    public void a() {
        tileEntityChunkLoader.tick();
    }

    @Override
    public boolean b() {
        return tileEntityChunkLoader.isRemoved();
    }

    @Override
    public BlockPosition c() {
        return tileEntityChunkLoader.getPosition();
    }

    @Override
    public String d() {
        return TileEntityTypes.a(tileEntityChunkLoader.getTileType()) + "";
    }
}
