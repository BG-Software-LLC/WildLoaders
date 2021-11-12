package com.bgsoftware.wildloaders.handlers;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.managers.NPCManager;
import com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC;
import com.bgsoftware.wildloaders.npc.NPCIdentifier;
import com.bgsoftware.wildloaders.utils.database.Query;
import com.google.common.collect.Maps;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class NPCHandler implements NPCManager {

    private final WildLoadersPlugin plugin;
    private final Map<NPCIdentifier, ChunkLoaderNPC> npcs = Maps.newConcurrentMap();
    private final Map<NPCIdentifier, UUID> npcUUIDs = Maps.newConcurrentMap();

    public NPCHandler(WildLoadersPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public Optional<ChunkLoaderNPC> getNPC(Location location) {
        return Optional.ofNullable(npcs.get(new NPCIdentifier(location)));
    }

    @Override
    public ChunkLoaderNPC createNPC(Location location) {
        return npcs.computeIfAbsent(new NPCIdentifier(location), i -> plugin.getNMSAdapter().createNPC(i.getSpawnLocation(), getUUID(i)));
    }

    @Override
    public boolean isNPC(LivingEntity livingEntity) {
        ChunkLoaderNPC npc = getNPC(livingEntity.getLocation()).orElse(null);
        return npc != null && npc.getUniqueId().equals(livingEntity.getUniqueId());
    }

    @Override
    public void killNPC(ChunkLoaderNPC npc) {
        NPCIdentifier identifier = new NPCIdentifier(npc.getLocation());
        npcs.remove(identifier);

        npcUUIDs.remove(identifier);

        Query.DELETE_NPC_IDENTIFIER.insertParameters()
                .setLocation(identifier.getSpawnLocation())
                .queue(npc.getUniqueId());

        npc.die();
    }

    @Override
    public void killAllNPCs() {
        for(ChunkLoaderNPC npc : npcs.values()){
            npc.die();
        }

        npcs.clear();
    }

    public Map<NPCIdentifier, ChunkLoaderNPC> getNPCs() {
        return Collections.unmodifiableMap(npcs);
    }

    public void registerUUID(Location location, UUID uuid){
        npcUUIDs.put(new NPCIdentifier(location), uuid);
    }

    private UUID getUUID(NPCIdentifier identifier){
        if(npcUUIDs.containsKey(identifier))
            return npcUUIDs.get(identifier);

        UUID uuid;

        do{
            uuid = UUID.randomUUID();
        }while(npcUUIDs.containsValue(uuid));

        npcUUIDs.put(identifier, uuid);

        Query.INSERT_NPC_IDENTIFIER.insertParameters()
                .setLocation(identifier.getSpawnLocation())
                .setObject(uuid.toString())
                .queue(uuid);

        return uuid;
    }

    public static String getName(String worldName) {
        String name = "Loader-" + worldName;
        return name.length() > 16 ? name.substring(0, 16) : name;
    }

}
