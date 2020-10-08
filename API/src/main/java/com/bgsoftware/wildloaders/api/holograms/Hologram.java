package com.bgsoftware.wildloaders.api.holograms;

import org.bukkit.entity.Entity;

public interface Hologram {

    /**
     * Get the armor-stand associated with the hologram.
     */
    Entity getEntity();

    /**
     * Set the hologram's name.
     * Using the setCustomName method of the entity will not work.
     * @param name The new name of the hologram.
     */
    void setHologramName(String name);

    /**
     * Remove the hologram.
     */
    void removeHologram();

}
