package com.bgsoftware.wildloaders.hooks;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.hooks.SpawnersProvider;
import mc.rellox.spawnerlegacy.SpawnerLegacy;
import mc.rellox.spawnerlegacy.api.APIInstance;
import mc.rellox.spawnerlegacy.api.event.EventExecutor;
import mc.rellox.spawnerlegacy.api.event.block.SpawnerPlaceEvent;
import mc.rellox.spawnerlegacy.api.event.upgrade.SpawnerUpgradeEvent;
import mc.rellox.spawnerlegacy.api.region.IBox;
import mc.rellox.spawnerlegacy.api.spawner.IGenerator;
import mc.rellox.spawnerlegacy.spawner.generator.ActiveGenerator;
import mc.rellox.spawnerlegacy.utility.region.SphereBox;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class SpawnersProvider_SpawnerLegacy implements SpawnersProvider {

    private static final ReflectField<IBox> GENERATOR_BOX = new ReflectField<>(
            ActiveGenerator.class, IBox.class, "box");

    private final WildLoadersPlugin plugin;
    private final APIInstance api;

    public SpawnersProvider_SpawnerLegacy(WildLoadersPlugin plugin) {
        this.plugin = plugin;
        this.api = SpawnerLegacy.instance().getAPI();
        this.api.register(SpawnerUpgradeEvent.class, new SpawnerUpgradeListener());
        this.api.register(SpawnerPlaceEvent.class, new SpawnerPlaceListener());
    }

    @Override
    public void setSpawnerRequiredRange(Location spawnerLocation, int requiredRange) {
        updateBoxForGenerator(spawnerLocation.getBlock(), requiredRange, true);
    }

    private void updateBoxForGenerator(Block block, int requiredRange, boolean retryOnNull) {
        IGenerator generator = this.api.getGenerator(block);
        if (generator == null) {
            if (retryOnNull)
                Bukkit.getScheduler().runTaskLater(plugin, () -> updateBoxForGenerator(block, requiredRange, false), 20L);
            return;
        }

        IBox box;
        if (requiredRange == -1) {
            box = new LoaderBox(block.getX(), block.getY(), block.getZ(), generator.cache().range());
        } else {
            box = IBox.sphere(block, generator.cache().range());
        }

        GENERATOR_BOX.set(generator, box);
    }

    private class SpawnerUpgradeListener implements EventExecutor<SpawnerUpgradeEvent> {

        @Override
        public void execute(SpawnerUpgradeEvent e) {
            if (!e.cancelled())
                setLoaderBox(e.getGenerator(), true);
        }

    }

    private class SpawnerPlaceListener implements EventExecutor<SpawnerPlaceEvent> {

        @Override
        public void execute(SpawnerPlaceEvent e) {
            if (e.cancelled())
                return;

            Block block = e.block();

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                IGenerator generator = api.getGenerator(block);
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

                Block block = generator.block();

                GENERATOR_BOX.set(generator, new LoaderBox(block.getX(), block.getY(),
                        block.getZ(), generator.cache().range()));
            }, 1L);
        } else {
            Block block = generator.block();

            GENERATOR_BOX.set(generator, new LoaderBox(block.getX(), block.getY(),
                    block.getZ(), generator.cache().range()));
        }
    }

    private static class LoaderBox extends SphereBox {

        public LoaderBox(int x, int z, int y, int r) {
            super(x, y, z, r);
        }

        @Override
        public boolean in(Player player) {
            return true;
        }

        @Override
        public boolean any(Iterable<? extends Player> players) {
            return true;
        }
    }

}
