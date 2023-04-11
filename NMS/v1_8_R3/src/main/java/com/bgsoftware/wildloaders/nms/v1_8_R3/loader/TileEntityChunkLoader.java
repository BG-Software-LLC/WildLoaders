package com.bgsoftware.wildloaders.nms.v1_8_R3.loader;

import com.bgsoftware.wildloaders.api.holograms.Hologram;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.loaders.ITileEntityChunkLoader;
import com.bgsoftware.wildloaders.loaders.WChunkLoader;
import com.bgsoftware.wildloaders.nms.v1_8_R3.EntityHolograms;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IUpdatePlayerListBox;
import net.minecraft.server.v1_8_R3.TileEntity;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.craftbukkit.v1_8_R3.util.LongHash;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TileEntityChunkLoader extends TileEntity implements IUpdatePlayerListBox, ITileEntityChunkLoader {

    public static final Map<Long, TileEntityChunkLoader> tileEntityChunkLoaderMap = new HashMap<>();

    public final List<EntityHolograms> holograms = new ArrayList<>();
    private final WChunkLoader chunkLoader;
    private final Block loaderBlock;

    private short currentTick = 20;
    private short daysAmount, hoursAmount, minutesAmount, secondsAmount;
    public boolean removed = false;

    public TileEntityChunkLoader(ChunkLoader chunkLoader, World world, BlockPosition blockPosition) {
        this.chunkLoader = (WChunkLoader) chunkLoader;

        a(blockPosition);
        a(world);

        loaderBlock = world.getType(blockPosition).getBlock();

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

        List<String> hologramLines = this.chunkLoader.getHologramLines();

        double currentY = position.getY() + 1;
        for (int i = hologramLines.size(); i > 0; i--) {
            EntityHolograms hologram = new EntityHolograms(world, position.getX() + 0.5, currentY, position.getZ() + 0.5);
            updateName(hologram, hologramLines.get(i - 1));
            world.addEntity(hologram);
            currentY += 0.23;
            holograms.add(hologram);
        }
    }

    @Override
    public void c() {
        if (removed || ++currentTick <= 20)
            return;

        currentTick = 0;

        if (chunkLoader.isNotActive() || world.getType(position).getBlock() != loaderBlock) {
            chunkLoader.remove();
            return;
        }

        if (chunkLoader.isInfinite())
            return;

        List<String> hologramLines = chunkLoader.getHologramLines();

        int hologramsAmount = holograms.size();
        for (int i = hologramsAmount; i > 0; i--) {
            EntityHolograms hologram = holograms.get(hologramsAmount - i);
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

    private void updateName(EntityHolograms hologram, String line) {
        String placerName = Optional.ofNullable(chunkLoader.getWhoPlaced().getName()).orElse("");
        hologram.setHologramName(line
                .replace("{0}", placerName)
                .replace("{1}", daysAmount + "")
                .replace("{2}", hoursAmount + "")
                .replace("{3}", minutesAmount + "")
                .replace("{4}", secondsAmount + "")
        );
    }

}

