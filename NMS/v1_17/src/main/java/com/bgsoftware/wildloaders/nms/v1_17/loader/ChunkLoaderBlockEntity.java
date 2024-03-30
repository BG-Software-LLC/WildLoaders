package com.bgsoftware.wildloaders.nms.v1_17.loader;

import com.bgsoftware.wildloaders.api.holograms.Hologram;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.loaders.ITileEntityChunkLoader;
import com.bgsoftware.wildloaders.loaders.WChunkLoader;
import com.bgsoftware.wildloaders.nms.v1_17.EntityHologram;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ChunkLoaderBlockEntity extends BlockEntity implements ITileEntityChunkLoader {

    public static final Map<Long, ChunkLoaderBlockEntity> chunkLoaderBlockEntityMap = new HashMap<>();

    public final List<EntityHologram> holograms = new ArrayList<>();
    private final WChunkLoader chunkLoader;
    private final Block loaderBlock;
    private final ChunkLoaderBlockEntityTicker ticker;
    private final ServerLevel serverLevel;
    private final BlockPos blockPos;
    private final String cachedPlacerName;

    private short currentTick = 20;
    private short daysAmount, hoursAmount, minutesAmount, secondsAmount;
    public boolean removed = false;

    public ChunkLoaderBlockEntity(ChunkLoader chunkLoader, ServerLevel serverLevel, BlockPos blockPos) {
        super(BlockEntityType.COMMAND_BLOCK, blockPos, serverLevel.getBlockState(blockPos));

        this.chunkLoader = (WChunkLoader) chunkLoader;
        this.ticker = new ChunkLoaderBlockEntityTicker(this);
        this.blockPos = blockPos;
        this.serverLevel = serverLevel;

        setLevel(serverLevel);

        loaderBlock = serverLevel.getBlockState(blockPos).getBlock();

        this.cachedPlacerName = Optional.ofNullable(this.chunkLoader.getWhoPlaced().getName()).orElse("");

        if (!this.chunkLoader.isInfinite()) {
            long timeLeft = chunkLoader.getTimeLeft();

            daysAmount = (short) (timeLeft / 86400);
            timeLeft = timeLeft % 86400;

            hoursAmount = (short) (timeLeft / 3600);
            timeLeft = timeLeft % 3600;

            minutesAmount = (short) (timeLeft / 60);
            timeLeft = timeLeft % 60;

            secondsAmount = (short) timeLeft;
        }

        long chunkPosLong = ChunkPos.asLong(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        chunkLoaderBlockEntityMap.put(chunkPosLong, this);

        List<String> hologramLines = this.chunkLoader.getHologramLines();

        double currentY = blockPos.getY() + 1;
        for (int i = hologramLines.size(); i > 0; i--) {
            EntityHologram hologram = new EntityHologram(serverLevel, blockPos.getX() + 0.5, currentY, blockPos.getZ() + 0.5);
            updateName(hologram, hologramLines.get(i - 1));
            serverLevel.addFreshEntity(hologram);
            currentY += 0.23;
            holograms.add(hologram);
        }
    }

    public void tick() {
        if (removed || ++currentTick <= 20)
            return;

        currentTick = 0;

        if (chunkLoader.isNotActive() || this.serverLevel.getBlockState(this.blockPos).getBlock() != loaderBlock) {
            chunkLoader.remove();
            return;
        }

        if (chunkLoader.isInfinite())
            return;

        List<String> hologramLines = chunkLoader.getHologramLines();

        int hologramsAmount = holograms.size();
        for (int i = hologramsAmount; i > 0; i--) {
            EntityHologram hologram = holograms.get(hologramsAmount - i);
            updateName(hologram, hologramLines.get(i - 1));
        }

        chunkLoader.tick();

        if (!removed) {
            secondsAmount--;
            if (secondsAmount < 0) {
                secondsAmount = 59;
                minutesAmount--;
                if (minutesAmount < 0) {
                    minutesAmount = 59;
                    hoursAmount--;
                    if (hoursAmount < 0) {
                        hoursAmount = 23;
                        daysAmount--;
                    }
                }
            }
        }
    }

    @Override
    public Collection<Hologram> getHolograms() {
        return Collections.unmodifiableList(holograms);
    }

    @Override
    public boolean isRemoved() {
        return removed || super.isRemoved();
    }

    public ChunkLoaderBlockEntityTicker getTicker() {
        return ticker;
    }

    private void updateName(EntityHologram hologram, String line) {
        hologram.setHologramName(line
                .replace("{0}", this.cachedPlacerName)
                .replace("{1}", daysAmount + "")
                .replace("{2}", hoursAmount + "")
                .replace("{3}", minutesAmount + "")
                .replace("{4}", secondsAmount + "")
        );
    }

}

