package com.bgsoftware.wildloaders.nms.v1_16_R3.loader;

import com.bgsoftware.wildloaders.api.holograms.Hologram;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.loaders.ITileEntityChunkLoader;
import com.bgsoftware.wildloaders.loaders.WChunkLoader;
import com.bgsoftware.wildloaders.nms.v1_16_R3.EntityHolograms;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R3.ITickable;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.TileEntityTypes;
import net.minecraft.server.v1_16_R3.World;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public final class TileEntityChunkLoader extends TileEntity implements ITickable, ITileEntityChunkLoader {

    public static final Long2ObjectMap<TileEntityChunkLoader> tileEntityChunkLoaderMap = new Long2ObjectOpenHashMap<>();

    public final Int2ObjectMap<EntityHolograms> holograms = new Int2ObjectArrayMap<>();
    private final WChunkLoader chunkLoader;
    private final Block loaderBlock;
    private final String cachedPlacerName;

    private short currentTick = 20;
    private short daysAmount, hoursAmount, minutesAmount, secondsAmount;
    public boolean removed = false;

    public TileEntityChunkLoader(ChunkLoader chunkLoader, World world, BlockPosition blockPosition) {
        super(TileEntityTypes.COMMAND_BLOCK);

        this.chunkLoader = (WChunkLoader) chunkLoader;

        setLocation(world, blockPosition);

        loaderBlock = world.getType(blockPosition).getBlock();

        try {
            // Not a method of Spigot - fixes https://github.com/OmerBenGera/WildLoaders/issues/2
            setCurrentChunk(world.getChunkAtWorldCoords(blockPosition));
        } catch (Throwable ignored) {
        }

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

        tileEntityChunkLoaderMap.put(ChunkCoordIntPair.pair(blockPosition.getX() >> 4, blockPosition.getZ() >> 4), this);

        double baseYLevel = position.getY() + 1;
        this.chunkLoader.forEachHologramLine((index, hologramLine) -> {
            if (!hologramLine.isEmpty()) {
                double currentY = baseYLevel + (index * 0.23);
                EntityHolograms hologram = new EntityHolograms(world, position.getX() + 0.5, currentY, position.getZ() + 0.5);
                updateName(hologram, hologramLine);
                world.addEntity(hologram);
                this.holograms.put(index, hologram);
            }
        });
    }

    @Override
    public void w() {
        tick();
    }

    @Override
    public void tick() {
        if (removed || ++currentTick <= 20)
            return;

        currentTick = 0;

        assert world != null;
        if (chunkLoader.isNotActive() || world.getType(position).getBlock() != loaderBlock) {
            chunkLoader.remove();
            return;
        }

        if (chunkLoader.isInfinite())
            return;

        this.chunkLoader.forEachHologramLine((index, hologramLine) -> {
            if (!hologramLine.isEmpty()) {
                EntityHolograms hologram = this.holograms.get(index);
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

    private void updateName(EntityHolograms hologram, String line) {
        hologram.setHologramName(line
                .replace("{0}", this.cachedPlacerName)
                .replace("{1}", daysAmount + "")
                .replace("{2}", hoursAmount + "")
                .replace("{3}", minutesAmount + "")
                .replace("{4}", secondsAmount + "")
        );
    }

}

