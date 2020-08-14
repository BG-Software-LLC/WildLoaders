package com.bgsoftware.wildloaders.api.npc;

import org.bukkit.Location;

import java.util.UUID;

/**
 * NPCs are used to make spawners work in active chunks even without nearby players, and make crops to grow in newer versions.
 */
public interface ChunkLoaderNPC {

    /**
     * Get the unique id of the npc.
     */
    UUID getUniqueId();

    /**
     * Kill the npc.
     * It's recommended to use NPCManager#killNPC instead!
     */
    void die();

    /**
     * Get the location of the NPC.
     */
    Location getLocation();

}
