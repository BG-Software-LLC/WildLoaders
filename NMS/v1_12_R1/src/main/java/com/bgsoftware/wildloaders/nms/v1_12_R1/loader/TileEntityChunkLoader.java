package com.bgsoftware.wildloaders.nms.v1_12_R1.loader;

import com.bgsoftware.wildloaders.api.holograms.Hologram;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.loaders.ITileEntityChunkLoader;
import com.bgsoftware.wildloaders.loaders.WChunkLoader;
import com.bgsoftware.wildloaders.nms.v1_12_R1.EntityHolograms;
import net.minecraft.server.v1_12_R1.Block;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.ITickable;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.bukkit.craftbukkit.v1_12_R1.util.LongHash;

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
        this.chunkLoader = (WChunkLoader) chunkLoader;

        setPosition(blockPosition);
        a(world);

        loaderBlock = world.getType(blockPosition).getBlock();

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

        tileEntityChunkLoaderMap.put(LongHash.toLong(blockPosition.getX() >> 4, blockPosition.getZ() >> 4), this);

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
    public void e() {
        if (removed || ++currentTick <= 20)
            return;

        currentTick = 0;

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

