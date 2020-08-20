package com.bgsoftware.wildloaders.npc;

import com.bgsoftware.wildloaders.utils.ServerVersion;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public final class NPCIdentifier {

    private static final boolean PER_WORLD_NPCS = ServerVersion.isLessThan(ServerVersion.v1_14);

    private final Object identifier;

    public NPCIdentifier(Location location){
        this.identifier = PER_WORLD_NPCS ? location.getWorld() : location;
    }

    public Location getSpawnLocation(){
        return PER_WORLD_NPCS ? new Location((World) identifier, 0, 1, 0) : (Location) identifier;
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
