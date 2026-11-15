package com.bgsoftware.wildloaders.listeners;

import com.bgsoftware.wildloaders.Locale;
import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.loaders.ChunkLoader;
import com.bgsoftware.wildloaders.api.loaders.LoaderData;
import com.bgsoftware.wildloaders.utils.ChunkLoaderChunks;
import com.bgsoftware.wildloaders.utils.SpawnerChangeListener;
import com.bgsoftware.wildloaders.utils.chunks.ChunkPosition;
import com.bgsoftware.wildloaders.utils.legacy.Materials;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
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
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
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

        List<ChunkPosition> loaderChunks = ChunkLoaderChunks.calculateChunks(loaderData, e.getPlayer().getUniqueId(), e.getBlock().getLocation());
        for (ChunkPosition chunkPosition : loaderChunks) {
            if (plugin.getLoaders().getChunkLoader(chunkPosition).isPresent()) {
                e.setCancelled(true);
                Locale.ALREADY_LOADED.send(e.getPlayer());
                return;
            }
        }

        long timeLeft = plugin.getNMSAdapter().getTag(e.getItemInHand(), "loader-time", loaderData.getTimeLeft());

        plugin.getLoaders().addChunkLoader(loaderData, e.getPlayer(), e.getBlock().getLocation(), timeLeft, loaderChunks);

        Locale.PLACED_LOADER.send(e.getPlayer(), ChunkPosition.of(e.getBlock().getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLoaderBreak(BlockBreakEvent e) {
        ChunkLoader chunkLoader = plugin.getLoaders().getChunkLoader(e.getBlock().getLocation()).orElse(null);
        if (chunkLoader == null)
            return;

        e.setCancelled(true);
        if (handleLoaderBreak(chunkLoader, e.getPlayer().getGameMode() != GameMode.CREATIVE))
            Locale.BROKE_LOADER.send(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLoaderExplode(EntityExplodeEvent e) {
        EntityType entityType = e.getEntityType();
        if (entityType == WIND_CHARGE_TYPE || entityType == BREEZE_WIND_CHARGE_TYPE)
            return;

        Iterator<Block> blocks = e.blockList().iterator();
        while (blocks.hasNext()) {
            Block block = blocks.next();
            ChunkLoader chunkLoader = plugin.getLoaders().getChunkLoader(block.getLocation()).orElse(null);
            if (chunkLoader == null)
                continue;

            blocks.remove();
            handleLoaderBreak(chunkLoader, true);
        }
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

    private boolean handleLoaderBreak(ChunkLoader chunkLoader, boolean dropItem) {
        Location blockLoc = chunkLoader.getLocation();
        ItemStack loaderItem = dropItem ? chunkLoader.getLoaderItem() : null;
        if (dropItem && (loaderItem == null || loaderItem.getType() == Material.AIR || loaderItem.getAmount() <= 0)) {
            plugin.getLogger().warning("Cannot break chunk-loader at " + blockLoc + ": its return item is empty.");
            return false;
        }

        chunkLoader.remove();

        if (dropItem) {
            Item item = blockLoc.getWorld().dropItem(blockLoc.clone().add(0.5, 0.5, 0.5), loaderItem);
            if (!item.isValid()) {
                plugin.getLogger().warning("The return item for chunk-loader at " + blockLoc +
                        " did not spawn; another plugin may have cancelled or handled its spawn.");
            }
        }

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
