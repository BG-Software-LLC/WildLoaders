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
import net.minecraft.world.level.IWorldWriter;
import net.minecraft.world.level.MobSpawnerAbstract;
import net.minecraft.world.level.World;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.block.entity.TileEntityTypes;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.IChunkAccess;

import java.util.Map;

public final class NMSMappings_v1_19_R1 {

    private static final ReflectMethod<Void> SET_GAMEMODE = new ReflectMethod<>(PlayerInteractManager.class,
            1, EnumGamemode.class, EnumGamemode.class);

    private NMSMappings_v1_19_R1() {

    }

    public static NBTTagCompound getOrCreateTag(ItemStack itemStack) {
        return itemStack.v();
    }

    public static boolean contains(NBTTagCompound nbtTagCompound, String key, int type) {
        return nbtTagCompound.b(key, type);
    }

    public static String getString(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.l(key);
    }

    public static void put(NBTTagCompound nbtTagCompound, String key, NBTBase nbtBase) {
        nbtTagCompound.a(key, nbtBase);
    }

    public static long getLong(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.i(key);
    }

    public static boolean contains(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.e(key);
    }

    public static NBTTagCompound getCompound(NBTTagCompound nbtTagCompound, String key) {
        return nbtTagCompound.p(key);
    }

    public static void putString(NBTTagCompound nbtTagCompound, String key, String value) {
        nbtTagCompound.a(key, value);
    }

    public static Map<BlockPosition, TileEntity> getBlockEntities(IChunkAccess chunkAccess) {
        return chunkAccess.i;
    }

    public static ChunkCoordIntPair getPos(IChunkAccess chunk) {
        return chunk.f();
    }

    public static MobSpawnerAbstract getSpawner(TileEntityMobSpawner tileEntityMobSpawner) {
        return tileEntityMobSpawner.d();
    }

    public static void setChunkForced(WorldServer worldServer, int chunkX, int chunkZ, boolean load) {
        worldServer.a(chunkX, chunkZ, load);
    }

    public static void addNewPlayer(WorldServer worldServer, EntityPlayer entityPlayer) {
        worldServer.c(entityPlayer);
    }

    public static int getId(IBlockData blockData) {
        return Block.i(blockData);
    }

    public static Block getBlock(IBlockData blockData) {
        return blockData.b();
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

    public static IBlockData getBlockState(World world, BlockPosition blockPosition) {
        return world.a_(blockPosition);
    }

    public static TileEntity getBlockEntity(World world, BlockPosition blockPosition) {
        return world.c_(blockPosition);
    }

    public static void addFreshEntity(IWorldWriter worldWriter, Entity entity) {
        worldWriter.b(entity);
    }

    public static BlockPosition getBlockPos(TileEntity tileEntity) {
        return tileEntity.p();
    }

    public static TileEntityTypes<?> getType(TileEntity tileEntity) {
        return tileEntity.v();
    }

    public static void setGameModeForPlayer(PlayerInteractManager playerInteractManager, EnumGamemode gamemode) {
        SET_GAMEMODE.invoke(playerInteractManager, gamemode, null);
    }

    public static World getWorld(Entity entity) {
        return entity.W();
    }

    public static void moveTo(Entity entity, double x, double y, double z, float yaw, float pitch) {
        entity.b(x, y, z, yaw, pitch);
    }

    public static WorldServer getLevel(EntityPlayer entityPlayer) {
        return entityPlayer.x();
    }

}
