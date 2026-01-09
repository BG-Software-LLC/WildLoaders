package com.bgsoftware.wildloaders.utils.chunks;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Objects;

public final class ChunkPosition {

    private final String world;
    private final int x, z;

    private long pairedXZ = -1;

    private WeakReference<World> bukkitWorld;

    private ChunkPosition(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public String getWorld() {
        return world;
    }

    @Nullable
    public World getBukkitWorld() {
        World bukkitWorld = this.bukkitWorld.get();
        return bukkitWorld == null ? Bukkit.getWorld(this.world) : bukkitWorld;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public long asPair() {
        if (this.pairedXZ < 0)
            pairedXZ = pair(this.x, this.z);

        return pairedXZ;
    }

    ChunkPosition setBukkitWorld(World world) {
        this.bukkitWorld = new WeakReference<>(world);
        return this;
    }

    @Override
    public String toString() {
        return world + ", " + x + ", " + z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPosition that = (ChunkPosition) o;
        return x == that.x &&
                z == that.z &&
                world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }

    public static ChunkPosition of(Location location) {
        return of(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public static ChunkPosition of(Chunk chunk) {
        return of(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    public static ChunkPosition of(World world, int chunkX, int chunkZ) {
        return new ChunkPosition(world.getName(), chunkX, chunkZ).setBukkitWorld(world);
    }

    public static ChunkPosition of(String worldName, int chunkX, int chunkZ) {
        return new ChunkPosition(worldName, chunkX, chunkZ);
    }

    public static long pair(int x, int z) {
        return (long) x & 4294967295L | ((long) z & 4294967295L) << 32;
    }

}
