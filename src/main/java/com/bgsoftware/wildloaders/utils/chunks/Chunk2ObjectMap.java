package com.bgsoftware.wildloaders.utils.chunks;

import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class Chunk2ObjectMap<V> extends AbstractMap<ChunkPosition, V> {

    private final Map<String, Map<Long, V>> backendMap = new LinkedHashMap<>();
    private int size = 0;

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return value != null && super.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return key instanceof ChunkPosition ? get((ChunkPosition) key) : null;
    }

    @Nullable
    public V get(ChunkPosition chunkPosition) {
        return get(chunkPosition.getWorld(), chunkPosition.asPair());
    }

    @Nullable
    public V get(String worldName, long chunkPair) {
        Map<Long, V> worldBackendData = this.backendMap.get(worldName);
        if (worldBackendData == null)
            return null;

        return worldBackendData.get(chunkPair);
    }

    public V computeIfAbsent(String worldName, long chunkPair, Supplier<V> newValue) {
        Map<Long, V> worldBackendData = this.backendMap.computeIfAbsent(worldName, n -> new LinkedHashMap<>());

        return worldBackendData.computeIfAbsent(chunkPair, p -> {
            ++Chunk2ObjectMap.this.size;
            return newValue.get();
        });
    }

    @Nullable
    @Override
    public V put(ChunkPosition chunkPosition, V value) {
        return put(chunkPosition.getWorld(), chunkPosition.asPair(), value);
    }

    @Nullable
    public V put(String worldName, long chunkPair, V value) {
        Map<Long, V> worldBackendData = this.backendMap.computeIfAbsent(worldName, n -> new LinkedHashMap<>());

        V oldValue = worldBackendData.put(chunkPair, value);

        if (oldValue == null)
            ++this.size;

        return oldValue;
    }

    @Override
    public V remove(Object key) {
        return key instanceof ChunkPosition ? remove((ChunkPosition) key) : null;
    }

    @Nullable
    public V remove(ChunkPosition chunkPosition) {
        return remove(chunkPosition.getWorld(), chunkPosition.asPair());
    }

    @Nullable
    public V remove(String worldName, long chunkPair) {
        Map<Long, V> worldBackendData = this.backendMap.get(worldName);
        if (worldBackendData == null)
            return null;

        V oldValue = onRemove(worldBackendData.remove(chunkPair));
        if (oldValue == null)
            return null;

        if (worldBackendData.isEmpty())
            Chunk2ObjectMap.this.backendMap.remove(worldName);

        return oldValue;
    }

    @Nullable
    private V onRemove(@Nullable V removedValue) {
        if (removedValue != null)
            --this.size;
        return removedValue;
    }

    @Override
    public void clear() {
        this.backendMap.clear();
        this.size = 0;
    }

    @Override
    public @NotNull Set<ChunkPosition> keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Collection<V> values() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Set<Entry<ChunkPosition, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

}
