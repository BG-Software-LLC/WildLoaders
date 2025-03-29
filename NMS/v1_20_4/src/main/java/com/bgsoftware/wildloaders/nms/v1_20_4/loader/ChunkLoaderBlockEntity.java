package com.bgsoftware.wildloaders.nms.v1_20_4.loader;

import com.bgsoftware.wildloaders.api.holograms.Hologram;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.loaders.ITileEntityChunkLoader;
import com.bgsoftware.wildloaders.loaders.WChunkLoader;
import com.bgsoftware.wildloaders.nms.v1_20_4.EntityHologram;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public final class ChunkLoaderBlockEntity extends BlockEntity implements ITileEntityChunkLoader {

    public static final Long2ObjectMap<ChunkLoaderBlockEntity> chunkLoaderBlockEntityMap = new Long2ObjectOpenHashMap<>();

    public final Int2ObjectMap<EntityHologram> holograms = new Int2ObjectArrayMap<>();
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

        double baseYLevel = blockPos.getY() + 1;
        this.chunkLoader.forEachHologramLine((index, hologramLine) -> {
            if (!hologramLine.isEmpty()) {
                double currentY = baseYLevel + (index * 0.23);
                EntityHologram hologram = new EntityHologram(serverLevel, blockPos.getX() + 0.5, currentY, blockPos.getZ() + 0.5);
                updateName(hologram, hologramLine);
                serverLevel.addFreshEntity(hologram);
                this.holograms.put(index, hologram);
            }
        });
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

        this.chunkLoader.forEachHologramLine((index, hologramLine) -> {
            if (!hologramLine.isEmpty()) {
                EntityHologram hologram = this.holograms.get(index);
                updateName(hologram, hologramLine);
            }
        });

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
        return Collections.unmodifiableCollection(this.holograms.values());
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

