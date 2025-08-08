package com.bgsoftware.wildloaders.hooks;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.hooks.SpawnersProvider;
import mc.rellox.spawnerlegacyapi.ISpawnerLegacy;
import mc.rellox.spawnerlegacyapi.SLAPI;
import mc.rellox.spawnerlegacyapi.event.EventExecutor;
import mc.rellox.spawnerlegacyapi.event.block.SpawnerPlaceEvent;
import mc.rellox.spawnerlegacyapi.event.upgrade.SpawnerUpgradeEvent;
import mc.rellox.spawnerlegacyapi.spawner.IGenerator;
import mc.rellox.spawnerlegacyapi.spawner.IGeneratorTags;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class SpawnersProvider_SpawnerLegacy2 implements SpawnersProvider {

    private final WildLoadersPlugin plugin;
    private final ISpawnerLegacy api;

    public SpawnersProvider_SpawnerLegacy2(WildLoadersPlugin plugin) {
        this.plugin = plugin;
        this.api = SLAPI.get();
        this.api.events().register(SpawnerUpgradeEvent.class, new SpawnerUpgradeListener());
        this.api.events().register(SpawnerPlaceEvent.class, new SpawnerPlaceListener());
    }

    @Override
    public void setSpawnerRequiredRange(Location spawnerLocation, int requiredRange) {
        updatePlayerRequirementForGenerator(spawnerLocation.getBlock(), requiredRange != -1, true);
    }

    private void updatePlayerRequirementForGenerator(Block block, boolean enableRequirement, boolean retryOnNull) {
        IGenerator generator = this.api.generators().get(block, false);
        if (generator == null) {
            if (retryOnNull)
                Bukkit.getScheduler().runTaskLater(plugin, () -> updatePlayerRequirementForGenerator(block, enableRequirement, false), 20L);
            return;
        }

        updatePlayerRequirementForGenerator(generator, enableRequirement);
    }

    private void updatePlayerRequirementForGenerator(IGenerator generator, boolean enableRequirement) {
        if (enableRequirement) {
            generator.untag(IGeneratorTags.Tag.PLAYER_REQUIREMENT_DISABLE);
        } else {
            generator.tag(IGeneratorTags.Tag.PLAYER_REQUIREMENT_DISABLE, true);
        }
    }

    private class SpawnerUpgradeListener implements EventExecutor<SpawnerUpgradeEvent> {

        @Override
        public void execute(SpawnerUpgradeEvent e) {
            if (!e.cancelled())
                setLoaderBox(e.generator(), true);
        }

    }

    private class SpawnerPlaceListener implements EventExecutor<SpawnerPlaceEvent> {

        @Override
        public void execute(SpawnerPlaceEvent e) {
            if (e.cancelled())
                return;

            Block block = e.block();

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                IGenerator generator = api.generators().get(block, false);
                if (generator != null)
                    setLoaderBox(generator, false);
            }, 5L);
        }

    }

    private void setLoaderBox(IGenerator generator, boolean delay) {
        Chunk chunk = generator.center().getChunk();

        if (plugin.getLoaders().getChunkLoader(chunk).isEmpty())
            return;

        if (delay) {
            // Chunk is loaded, we want to update the box in the next tick.
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Ensure chunk is still loaded.
                if (plugin.getLoaders().getChunkLoader(chunk).isEmpty())
                    return;

                updatePlayerRequirementForGenerator(generator, false);
            }, 1L);
        } else {
            updatePlayerRequirementForGenerator(generator, false);
        }
    }

}
