package com.bgsoftware.wildloaders.nms;

import com.bgsoftware.common.reflection.ReflectMethod;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerInteractManager;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.EnumGamemode;
import net.minecraft.world.level.MobSpawnerAbstract;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.IChunkAccess;

import java.util.Map;

public final class NMSMappings_v1_18_R2 {

    private static final ReflectMethod<Void> SET_GAMEMODE = new ReflectMethod<>(PlayerInteractManager.class,
            1, EnumGamemode.class, EnumGamemode.class);

    private NMSMappings_v1_18_R2() {

    }

    public static NBTTagCompound getOrCreateTag(ItemStack itemStack) {
        return itemStack.t();
    }

    public static boolean hasKeyOfType(NBTTagCompound nbtTagCompound, String key, int type) {
        return nbtTagCompound.b(key, type);
    }

    public static String getString(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.l(key);
    }

    public static void set(NBTTagCompound nbtTagCompound, String key, NBTBase nbtBase) {
        nbtTagCompound.a(key, nbtBase);
    }

    public static long getLong(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.i(key);
    }

    public static boolean hasKey(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.e(key);
    }

    public static NBTTagCompound getCompound(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.p(key);
    }

    public static void setString(NBTTagCompound nbtTagCompound, String key, String value) {
        nbtTagCompound.a(key, value);
    }

    public static Map<BlockPosition, TileEntity> getTileEntities(IChunkAccess chunkAccess) {
        return chunkAccess.i;
    }

    public static MobSpawnerAbstract getSpawner(TileEntityMobSpawner tileEntityMobSpawner) {
        return tileEntityMobSpawner.d();
    }

    public static void setForceLoaded(WorldServer worldServer, int chunkX, int chunkZ, boolean load) {
        worldServer.a(chunkX, chunkZ, load);
    }

    public static ChunkCoordIntPair getPos(IChunkAccess chunk) {
        return chunk.f();
    }

    public static int getCombinedId(IBlockData blockData) {
        return Block.i(blockData);
    }

    public static int getX(BaseBlockPosition baseBlockPosition) {
        return baseBlockPosition.u();
    }

    public static int getY(BaseBlockPosition baseBlockPosition) {
        return baseBlockPosition.v();
    }

    public static int getZ(BaseBlockPosition baseBlockPosition) {
        return baseBlockPosition.w();
    }

    public static IBlockData getType(World world, BlockPosition blockPosition) {
        return world.a_(blockPosition);
    }

    public static TileEntity getTileEntity(World world, BlockPosition blockPosition) {
        return world.c_(blockPosition);
    }

    public static Block getBlock(IBlockData blockData) {
        return blockData.b();
    }

    public static BlockPosition getPosition(TileEntity tileEntity) {
        return tileEntity.p();
    }

    public static void addEntity(World world, Entity entity) {
        world.b(entity);
    }

    public static TileEntityTypes<?> getTileType(TileEntity tileEntity) {
        return tileEntity.u();
    }

    public static void setGameMode(PlayerInteractManager playerInteractManager, EnumGamemode gamemode) {
        SET_GAMEMODE.invoke(playerInteractManager, gamemode, null);
    }

    public static World getWorld(Entity entity) {
        return entity.cA();
    }

    public static void setLocation(Entity entity, double x, double y, double z, float yaw, float pitch) {
        entity.a(x, y, z, yaw, pitch);
    }

    public static void addPlayerJoin(WorldServer worldServer, EntityPlayer entityPlayer) {
        worldServer.c(entityPlayer);
    }

    public static WorldServer getWorldServer(EntityPlayer entityPlayer) {
        return entityPlayer.x();
    }

}
