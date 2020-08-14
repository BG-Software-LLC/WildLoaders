package com.bgsoftware.wildloaders.api.managers;

import com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.Optional;

public interface NPCManager {

    /**
     * Get a NPC by it's location.
     * @param location The location of the npc.
     */
    Optional<ChunkLoaderNPC> getNPC(Location location);

    /**
     * Create a NPC in a specific location.
     * @param location The location of the npc.
     */
    ChunkLoaderNPC createNPC(Location location);

    /**
     * Check whether or not an entity is an NPC.
     * @param livingEntity The entity to check.
     */
    boolean isNPC(LivingEntity livingEntity);

    /**
     * Kill a NPC.
     * @param npc The NPC to kill.
     */
    void killNPC(ChunkLoaderNPC npc);

    /**
     * Kill all the npcs on the server.
     */
    void killAllNPCs();

}
