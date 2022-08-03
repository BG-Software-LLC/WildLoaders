package com.bgsoftware.wildloaders.nms.v1_7_R4.loader;

import com.bgsoftware.wildloaders.api.holograms.Hologram;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.loaders.ITileEntityChunkLoader;
import com.bgsoftware.wildloaders.loaders.WChunkLoader;
import net.minecraft.server.v1_7_R4.Block;
import net.minecraft.server.v1_7_R4.IUpdatePlayerListBox;
import net.minecraft.server.v1_7_R4.TileEntity;
import net.minecraft.server.v1_7_R4.World;
import org.bukkit.craftbukkit.v1_7_R4.util.LongHash;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class TileEntityChunkLoader extends TileEntity implements IUpdatePlayerListBox, ITileEntityChunkLoader {

    private static final Collection<Hologram> EMPTY_CONTAINER = Collections.emptyList();

    public static final Map<Long, TileEntityChunkLoader> tileEntityChunkLoaderMap = new HashMap<>();

    private final WChunkLoader chunkLoader;
    private final Block loaderBlock;

    private short currentTick = 20;
    public boolean removed = false;

    public TileEntityChunkLoader(ChunkLoader chunkLoader, World world, int x, int y, int z) {
        this.chunkLoader = (WChunkLoader) chunkLoader;

        this.x = x;
        this.y = y;
        this.z = z;
        a(world);

        loaderBlock = world.getType(x, y, z);

        tileEntityChunkLoaderMap.put(LongHash.toLong(x >> 4, z >> 4), this);
    }

    @Override
    public void a() {
        if (removed || ++currentTick <= 20)
            return;

        currentTick = 0;

        if (chunkLoader.isNotActive() || world.getType(x, y, z) != loaderBlock) {
            chunkLoader.remove();
            return;
        }

        chunkLoader.tick();
    }

    @Override
    public Collection<Hologram> getHolograms() {
        return EMPTY_CONTAINER;
    }

}
