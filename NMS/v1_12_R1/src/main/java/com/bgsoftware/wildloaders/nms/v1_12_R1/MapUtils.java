package com.bgsoftware.wildloaders.nms.v1_12_R1;

import com.bgsoftware.wildloaders.nms.v1_12_R1.loader.TileEntityChunkLoader;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class MapUtils {

    public static final Supplier<Map<Long, TileEntityChunkLoader>> LONG2LOADER_MAP_CREATOR = initializeLong2LoaderMapCreator();
    public static final Supplier<Map<Integer, EntityHolograms>> INT2HOLOGRAM_MAP_CREATOR = initializeInt2HologramMapCreator();

    private MapUtils() {

    }

    private static Supplier<Map<Long, TileEntityChunkLoader>> initializeLong2LoaderMapCreator() {
        Class<?> clazz = findClass("org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap",
                "it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap");
        if (clazz != null) {
            return () -> {
                try {
                    return (Map<Long, TileEntityChunkLoader>) clazz.newInstance();
                } catch (Throwable error) {
                    error.printStackTrace();
                    return new HashMap<>();
                }
            };
        }

        return HashMap::new;
    }

    private static Supplier<Map<Integer, EntityHolograms>> initializeInt2HologramMapCreator() {
        Class<?> clazz = findClass("org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap",
                "it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap");
        if (clazz != null) {
            return () -> {
                try {
                    return (Map<Integer, EntityHolograms>) clazz.newInstance();
                } catch (Throwable error) {
                    error.printStackTrace();
                    return new HashMap<>();
                }
            };
        }

        return HashMap::new;
    }

    @Nullable
    private static Class<?> findClass(String... classes) {
        for (String clazz : classes) {
            try {
                return Class.forName(clazz);
            } catch (ClassNotFoundException ignored) {
            }
        }

        return null;
    }

}
