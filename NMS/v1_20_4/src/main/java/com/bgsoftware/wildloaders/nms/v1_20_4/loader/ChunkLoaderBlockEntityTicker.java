package com.bgsoftware.wildloaders.nms.v1_20_4.loader;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

public record ChunkLoaderBlockEntityTicker(
        ChunkLoaderBlockEntity chunkLoaderBlockEntity) implements TickingBlockEntity {

    @Override
    public void tick() {
        chunkLoaderBlockEntity.tick();
    }

    @Override
    public boolean isRemoved() {
        return chunkLoaderBlockEntity.isRemoved();
    }

    @Override
    public BlockPos getPos() {
        return chunkLoaderBlockEntity.getBlockPos();
    }

    @Override
    public String getType() {
        return BlockEntityType.getKey(chunkLoaderBlockEntity.getType()) + "";
    }

    public BlockEntity getTileEntity() {
        return chunkLoaderBlockEntity;
    }

}
