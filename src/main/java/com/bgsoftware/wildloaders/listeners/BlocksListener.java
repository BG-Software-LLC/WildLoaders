package com.bgsoftware.wildloaders.listeners;

import com.bgsoftware.wildloaders.Locale;
import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.utils.ChunkLoaderChunks;
import com.bgsoftware.wildloaders.utils.SpawnerChangeListener;
import com.bgsoftware.wildloaders.utils.chunks.ChunkPosition;
import com.bgsoftware.wildloaders.utils.legacy.Materials;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.annotation.Nullable;
import java.util.Optional;

@SuppressWarnings("unused")
public final class BlocksListener implements Listener {

    private static final EntityType WIND_CHARGE_TYPE = lookupEntityType("WIND_CHARGE");
    private static final EntityType BREEZE_WIND_CHARGE_TYPE = lookupEntityType("BREEZE_WIND_CHARGE");

    private final WildLoadersPlugin plugin;

    public BlocksListener(WildLoadersPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLoaderPlace(BlockPlaceEvent e) {
        String loaderName = plugin.getNMSAdapter().getTag(e.getItemInHand(), "loader-name", "");
        Optional<LoaderData> optionalLoaderData = plugin.getLoaders().getLoaderData(loaderName);

        if (!optionalLoaderData.isPresent())
            return;

        if (!e.getPlayer().hasPermission("wildloaders.use")) {
            e.setCancelled(true);
            Locale.NO_PLACE_PERMISSION.send(e.getPlayer());
            return;
        }

        LoaderData loaderData = optionalLoaderData.get();

        for (Chunk chunk : ChunkLoaderChunks.calculateChunks(loaderData, e.getPlayer().getUniqueId(), e.getBlock().getLocation())) {
            if (plugin.getLoaders().getChunkLoader(chunk).isPresent()) {
                e.setCancelled(true);
                Locale.ALREADY_LOADED.send(e.getPlayer());
                return;
            }
        }

        long timeLeft = plugin.getNMSAdapter().getTag(e.getItemInHand(), "loader-time", loaderData.getTimeLeft());

        plugin.getLoaders().addChunkLoader(loaderData, e.getPlayer(), e.getBlock().getLocation(), timeLeft);

        Locale.PLACED_LOADER.send(e.getPlayer(), ChunkPosition.of(e.getBlock().getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLoaderBreak(BlockBreakEvent e) {
        if (handleLoaderBreak(e.getBlock(), e.getPlayer().getGameMode() != GameMode.CREATIVE)) {
            e.setCancelled(true);
            Locale.BROKE_LOADER.send(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLoaderExplode(EntityExplodeEvent e) {
        EntityType entityType = e.getEntityType();
        if (entityType == WIND_CHARGE_TYPE || entityType == BREEZE_WIND_CHARGE_TYPE)
            return;

        e.blockList().removeIf(block -> handleLoaderBreak(block, true));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSpawnerPlace(BlockPlaceEvent e) {
        if (e.getBlock().getType() != Materials.SPAWNER.toBukkitType())
            return;

        if (!plugin.getLoaders().getChunkLoader(e.getBlock().getChunk()).isPresent())
            return;

        plugin.getNMSAdapter().updateSpawner(e.getBlock().getLocation(), false, SpawnerChangeListener.CALLBACK);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLoaderInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (plugin.getLoaders().getChunkLoader(e.getClickedBlock().getLocation()).isPresent())
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLoaderPistonRetract(BlockPistonRetractEvent e) {
        try {
            for (Block block : e.getBlocks()) {
                if (plugin.getLoaders().getChunkLoader(block.getLocation()).isPresent()) {
                    e.setCancelled(true);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLoaderPistonExtend(BlockPistonExtendEvent e) {
        try {
            for (Block block : e.getBlocks()) {
                if (plugin.getLoaders().getChunkLoader(block.getLocation()).isPresent()) {
                    e.setCancelled(true);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private boolean handleLoaderBreak(Block block, boolean dropItem) {
        Location blockLoc = block.getLocation();
        Optional<ChunkLoader> optionalChunkLoader = plugin.getLoaders().getChunkLoader(blockLoc);

        if (!optionalChunkLoader.isPresent())
            return false;

        ChunkLoader chunkLoader = optionalChunkLoader.get();
        chunkLoader.remove();

        if (dropItem)
            blockLoc.getWorld().dropItemNaturally(blockLoc, chunkLoader.getLoaderItem());

        return true;
    }

    @Nullable
    private static EntityType lookupEntityType(String name) {
        try {
            return EntityType.valueOf(name);
        } catch (IllegalArgumentException error) {
            return null;
        }
    }

}
