package com.bgsoftware.wildloaders.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class BlockPosition {

    private final String worldName;
    private final int x;
    private final int y;
    private final int z;
    private WeakReference<World> cachedWorld = new WeakReference<>(null);

    public static BlockPosition of(Location location) {
        World world = location.getWorld();
        return new BlockPosition(world == null ? "null" : world.getName(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static BlockPosition deserialize(String serialized) {
        String[] locationSections = serialized.split(",");

        if (locationSections.length != 4)
            throw new IllegalArgumentException("Cannot parse location " + serialized);

        String worldName = locationSections[0];
        int x = (int) Double.parseDouble(locationSections[1]);
        int y = (int) Double.parseDouble(locationSections[2]);
        int z = (int) Double.parseDouble(locationSections[3]);

        return new BlockPosition(worldName, x, y, z);
    }

    public BlockPosition(String worldName, int x, int y, int z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getWorldName() {
        return worldName;
    }

    public World getWorld() {
        World cachedWorld = this.cachedWorld.get();
        if (cachedWorld == null) {
            cachedWorld = Bukkit.getWorld(this.worldName);
            this.cachedWorld = new WeakReference<>(cachedWorld);
        }

        return cachedWorld;
    }

    public Location getLocation() {
        return new Location(getWorld(), this.x, this.y, this.z);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String serialize() {
        return this.worldName + "," + this.x + "," + this.y + "," + this.z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockPosition that = (BlockPosition) o;
        return x == that.x && y == that.y && z == that.z && Objects.equals(worldName, that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, y, z);
    }

}
