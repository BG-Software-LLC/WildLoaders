package com.bgsoftware.wildloaders.loaders;

import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.utils.BlockPosition;

import java.util.UUID;

public class UnloadedChunkLoader {

    private final LoaderData loaderData;
    private final UUID placer;
    private final BlockPosition blockPosition;
    private final long timeLeft;

    public UnloadedChunkLoader(LoaderData loaderData, UUID placer, BlockPosition blockPosition, long timeLeft) {
        this.loaderData = loaderData;
        this.placer = placer;
        this.blockPosition = blockPosition;
        this.timeLeft = timeLeft;
    }

    public LoaderData getLoaderData() {
        return loaderData;
    }

    public UUID getPlacer() {
        return placer;
    }

    public BlockPosition getBlockPosition() {
        return blockPosition;
    }

    public long getTimeLeft() {
        return timeLeft;
    }

}
