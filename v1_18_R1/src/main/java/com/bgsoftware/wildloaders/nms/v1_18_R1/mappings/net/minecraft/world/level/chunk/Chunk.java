package com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.net.minecraft.world.level.chunk;

import com.bgsoftware.wildloaders.nms.mapping.Remap;
import com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.MappedObject;
import com.bgsoftware.wildloaders.nms.v1_18_R1.mappings.net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.block.entity.TileEntity;

import java.util.Map;

public class Chunk extends MappedObject<net.minecraft.world.level.chunk.Chunk> {

    public Chunk(net.minecraft.world.level.chunk.Chunk handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.LevelChunk",
            name = "getBlockEntities",
            type = Remap.Type.METHOD,
            remappedName = "E")
    public Map<BlockPosition, TileEntity> getBlockEntities() {
        return handle.E();
    }

    @Remap(classPath = "net.minecraft.world.level.chunk.ChunkAccess",
            name = "getPos",
            type = Remap.Type.METHOD,
            remappedName = "f")
    public ChunkCoordIntPair getPos() {
        return new ChunkCoordIntPair(handle.f());
    }

}
