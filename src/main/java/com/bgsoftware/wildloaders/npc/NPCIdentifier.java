package com.bgsoftware.wildloaders.npc;

import org.bukkit.Location;

import java.util.Objects;

public final class NPCIdentifier {

    private final Object identifier;

    public NPCIdentifier(Location location){
        this.identifier = getBlockLocation(location);
    }

    public Location getSpawnLocation(){
        return (Location) identifier;
    }

    @Override
    public String toString() {
        return identifier.toString();
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

    private static Location getBlockLocation(Location location){
        return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

}
