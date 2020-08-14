package com.bgsoftware.wildloaders.handlers;

import com.bgsoftware.wildloaders.WildLoadersPlugin;
import com.bgsoftware.wildloaders.api.managers.NPCManager;
import com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC;
import com.bgsoftware.wildloaders.utils.ServerVersion;
import com.bgsoftware.wildloaders.utils.locations.LocationUtils;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class NPCHandler implements NPCManager {

    private static final boolean PER_WORLD_NPCS = ServerVersion.isLessThan(ServerVersion.v1_14);

    private final WildLoadersPlugin plugin;
    private final Map<NPCIdentifier, ChunkLoaderNPC> npcs = Maps.newConcurrentMap();
    private final Map<NPCIdentifier, UUID> npcUUIDs = Maps.newConcurrentMap();

    public NPCHandler(WildLoadersPlugin plugin){
        this.plugin = plugin;
        loadUUIDs();
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
        if(!PER_WORLD_NPCS){
            NPCIdentifier identifier = new NPCIdentifier(npc.getLocation());
            npcs.remove(identifier);

            npcUUIDs.remove(identifier);
            saveUUIDs();

            npc.die();
        }
    }

    @Override
    public void killAllNPCs() {
        for(ChunkLoaderNPC npc : npcs.values()){
            npc.die();
        }

        npcs.clear();
    }

    private void loadUUIDs(){
        File file = new File(plugin.getDataFolder(), "uuids.yml");

        if(!file.exists())
            return;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        for(String location : cfg.getConfigurationSection("").getKeys(false)){
            try{
                Location _location = LocationUtils.getLocation(location);
                npcUUIDs.put(new NPCIdentifier(_location), UUID.fromString(cfg.getString(location)));
            }catch(Exception ignored){}
        }
    }

    private UUID getUUID(NPCIdentifier identifier){
        if(npcUUIDs.containsKey(identifier))
            return npcUUIDs.get(identifier);

        UUID uuid;

        do{
            uuid = UUID.randomUUID();
        }while(npcUUIDs.containsValue(uuid));

        npcUUIDs.put(identifier, uuid);

        saveUUIDs();

        return uuid;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveUUIDs(){
        if(Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveUUIDs);
            return;
        }

        File file = new File(plugin.getDataFolder(), "uuids.yml");

        if(!file.exists())
            file.delete();

        YamlConfiguration cfg = new YamlConfiguration();

        for(Map.Entry<NPCIdentifier, UUID> entry : npcUUIDs.entrySet())
            cfg.set(LocationUtils.getLocation(entry.getKey().getSpawnLocation()), entry.getValue() + "");

        try{
            file.getParentFile().mkdirs();
            file.createNewFile();
            cfg.save(file);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private static final class NPCIdentifier{

        private final Object identifier;

        NPCIdentifier(Location location){
            this.identifier = PER_WORLD_NPCS ? location.getWorld() : location;
        }

        Location getSpawnLocation(){
            return PER_WORLD_NPCS ? ((World) identifier).getSpawnLocation() : (Location) identifier;
        }

        @Override
        public String toString() {
            return PER_WORLD_NPCS ? ((World) identifier).getName() : identifier.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NPCIdentifier that = (NPCIdentifier) o;
            return Objects.equals(identifier, that.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier);
        }

    }

}
