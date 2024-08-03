package com.bgsoftware.wildloaders.handlers;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.managers.NPCManager;
import com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC;
import com.bgsoftware.wildloaders.utils.BlockPosition;
import com.bgsoftware.wildloaders.utils.database.Query;
import com.google.common.collect.Maps;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class NPCHandler implements NPCManager {

    private static int NPCS_COUNTER = 0;

    private final WildLoadersPlugin plugin;
    private final Map<BlockPosition, ChunkLoaderNPC> npcs = Maps.newConcurrentMap();
    private final Map<BlockPosition, UUID> npcUUIDs = Maps.newConcurrentMap();


    public NPCHandler(WildLoadersPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Optional<ChunkLoaderNPC> getNPC(Location location) {
        return getNPC(BlockPosition.of(location));
    }

    public Optional<ChunkLoaderNPC> getNPC(BlockPosition blockPosition) {
        return Optional.ofNullable(npcs.get(blockPosition));
    }

    @Override
    public ChunkLoaderNPC createNPC(Location location) {
        return createNPC(BlockPosition.of(location));
    }

    public ChunkLoaderNPC createNPC(BlockPosition blockPosition) {
        return npcs.computeIfAbsent(blockPosition, i -> {
            ChunkLoaderNPC npc = plugin.getNMSAdapter().createNPC(i.getLocation(), getUUID(i));
            Entity npcEntity = npc.getPlayer();
            npcEntity.setMetadata("NPC", new FixedMetadataValue(plugin, true));
            return npc;
        });
    }

    @Override
    public boolean isNPC(LivingEntity livingEntity) {
        ChunkLoaderNPC npc = getNPC(livingEntity.getLocation()).orElse(null);
        return npc != null && npc.getUniqueId().equals(livingEntity.getUniqueId());
    }

    @Override
    public void killNPC(ChunkLoaderNPC npc) {
        BlockPosition blockPosition = BlockPosition.of(npc.getLocation());

        npcs.remove(blockPosition);
        npcUUIDs.remove(blockPosition);

        Query.DELETE_NPC_IDENTIFIER.insertParameters()
                .setLocation(blockPosition)
                .queue(npc.getUniqueId());

        Entity npcEntity = npc.getPlayer();
        npcEntity.removeMetadata("NPC", plugin);

        npc.die();
    }

    @Override
    public void killAllNPCs() {
        for (ChunkLoaderNPC npc : npcs.values()) {
            npc.die();
        }

        npcs.clear();
    }

    public Map<BlockPosition, ChunkLoaderNPC> getNPCs() {
        return Collections.unmodifiableMap(npcs);
    }

    public void registerUUID(BlockPosition blockPosition, UUID uuid) {
        npcUUIDs.put(blockPosition, uuid);
    }

    private UUID getUUID(BlockPosition blockPosition) {
        UUID uuid = npcUUIDs.get(blockPosition);
        if (uuid != null)
            return uuid;

        do {
            uuid = UUID.randomUUID();
        } while (npcUUIDs.containsValue(uuid));

        npcUUIDs.put(blockPosition, uuid);

        Query.INSERT_NPC_IDENTIFIER.insertParameters()
                .setLocation(blockPosition)
                .setObject(uuid.toString())
                .queue(uuid);

        return uuid;
    }

    public static String getName(String worldName) {
        return "Loader-" + (worldName.length() > 7 ? worldName.substring(0, 7) : worldName) + "-" + (NPCS_COUNTER++);
    }

}
