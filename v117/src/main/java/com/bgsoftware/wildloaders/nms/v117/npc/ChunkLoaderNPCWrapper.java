package com.bgsoftware.wildloaders.nms.v117.npc;

import com.bgsoftware.wildloaders.api.npc.ChunkLoaderNPC;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ChunkLoaderNPCWrapper implements ChunkLoaderNPC {

    private final ChunkLoaderPlayer chunkLoaderPlayer;

    public ChunkLoaderNPCWrapper(MinecraftServer minecraftServer, Location location, UUID uuid) {
        this.chunkLoaderPlayer = new ChunkLoaderPlayer(minecraftServer, location, uuid);
    }

    @Override
    public UUID getUniqueId() {
        return this.chunkLoaderPlayer.getUUID();
    }

    @Override
    public void die() {
        this.chunkLoaderPlayer.discard();
    }

    @Override
    public Location getLocation() {
        return getPlayer().getLocation();
    }

    @Override
    public Player getPlayer() {
        return this.chunkLoaderPlayer.getBukkitEntity();
    }

}
