package com.bgsoftware.wildloaders.listeners;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.utils.ServerVersion;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.List;

public final class BlockProtectionListener implements Listener {

    private final WildLoadersPlugin plugin;

    public BlockProtectionListener(WildLoadersPlugin plugin) {
        this.plugin = plugin;

        if (ServerVersion.isAtLeast(ServerVersion.v1_9))
            plugin.getServer().getPluginManager().registerEvents(new LevelChangeListener(), plugin);
        if (ServerVersion.isAtLeast(ServerVersion.v1_14))
            plugin.getServer().getPluginManager().registerEvents(new BlockChangesListener(), plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent e) {
        protectBlock(e.getBlock(), e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent e) {
        protectBlock(e.getBlock(), e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent e) {
        protectBlock(e.getBlock(), e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent e) {
        protectBlock(e.getBlock(), e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent e) {
        protectBlock(e.getBlock(), e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent e) {
        protectBlock(e.getBlock(), e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent e) {
        protectBlock(e.getBlock(), e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent e) {
        protectBlock(e.getBlock(), e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent e) {
        if (isLoader(e.getBlock()) || isLoader(e.getToBlock()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        protectBlock(e.getBlock(), e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent e) {
        protectBlocks(e.getBlocks(), e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFurnaceBurn(FurnaceBurnEvent e) {
        protectBlock(e.getBlock(), e);
    }

    private boolean isLoader(Block block) {
        return plugin.getLoaders().getChunkLoader(block.getLocation()).isPresent();
    }

    private void protectBlock(Block block, Cancellable e) {
        if (isLoader(block))
            e.setCancelled(true);
    }

    private void protectBlocks(List<BlockState> blocks, Cancellable e) {
        for (BlockState blockState : blocks) {
            if (isLoader(blockState.getBlock())) {
                e.setCancelled(true);
                return;
            }
        }
    }

    private class LevelChangeListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onLevelChange(CauldronLevelChangeEvent e) {
            protectBlock(e.getBlock(), e);
        }

    }

    private class BlockChangesListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onMoistureChange(MoistureChangeEvent e) {
            protectBlock(e.getBlock(), e);
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onFluidLevelChange(FluidLevelChangeEvent e) {
            protectBlock(e.getBlock(), e);
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onBlockFertilize(BlockFertilizeEvent e) {
            protectBlock(e.getBlock(), e);
            protectBlocks(e.getBlocks(), e);
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onSpongeAbsorb(SpongeAbsorbEvent e) {
            protectBlock(e.getBlock(), e);
            protectBlocks(e.getBlocks(), e);
        }

    }

}
