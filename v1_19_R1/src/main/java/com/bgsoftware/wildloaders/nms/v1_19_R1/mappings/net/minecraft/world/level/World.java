package com.bgsoftware.wildloaders.nms.v1_19_R1.mappings.net.minecraft.world.level;

import com.bgsoftware.wildloaders.nms.mapping.Remap;
import com.bgsoftware.wildloaders.nms.v1_19_R1.mappings.MappedObject;
import com.bgsoftware.wildloaders.nms.v1_19_R1.mappings.net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.entity.TileEntity;

public class World extends MappedObject<net.minecraft.world.level.World> {

    public World(net.minecraft.world.level.World handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.server.level.ServerLevel",
            name = "setChunkForced",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setChunkForced(int chunkX, int chunkZ, boolean load) {
        ((WorldServer) handle).a(chunkX, chunkZ, load);
    }

    @Remap(classPath = "net.minecraft.server.level.ServerLevel",
            name = "addNewPlayer",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public void addNewPlayer(EntityPlayer entityPlayer) {
        ((WorldServer) handle).c(entityPlayer);
    }

    @Remap(classPath = "net.minecraft.server.level.ServerLevel",
            name = "removePlayerImmediately",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void removePlayer(EntityPlayer entityPlayer, Entity.RemovalReason removalReason) {
        ((WorldServer) handle).a(entityPlayer, removalReason);
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getBlockState",
            type = Remap.Type.METHOD,
            remappedName = "a_")
    public net.minecraft.world.level.block.state.IBlockData getBlockStateNoMappings(BlockPosition blockPosition) {
        return handle.a_(blockPosition);
    }

    public IBlockData getBlockState(BlockPosition blockPosition) {
        return new IBlockData(getBlockStateNoMappings(blockPosition));
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "getBlockEntity",
            type = Remap.Type.METHOD,
            remappedName = "c_")
    public TileEntity getBlockEntity(BlockPosition blockPosition) {
        return handle.c_(blockPosition);
    }

    @Remap(classPath = "net.minecraft.world.level.LevelWriter",
            name = "addFreshEntity",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public void addFreshEntity(Entity entity) {
        handle.b(entity);
    }

    @Remap(classPath = "net.minecraft.world.level.Level",
            name = "addBlockEntityTicker",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void addBlockEntityTicker(TickingBlockEntity blockEntityTicker) {
        handle.a(blockEntityTicker);
    }

    @Remap(classPath = "net.minecraft.server.level.ServerLevel",
            name = "levelEvent",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void levelEvent(EntityPlayer entityPlayer, int i, BlockPosition blockPosition, int j) {
        handle.a(entityPlayer, i, blockPosition, j);
    }

}
