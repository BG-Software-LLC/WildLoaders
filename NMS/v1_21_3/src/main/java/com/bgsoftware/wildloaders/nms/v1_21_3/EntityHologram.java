package com.bgsoftware.wildloaders.nms.v1_21_3;

import com.bgsoftware.wildloaders.api.holograms.Hologram;
import com.bgsoftware.wildloaders.scheduler.Scheduler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftArmorStand;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.util.CraftChatMessage;

public final class EntityHologram extends ArmorStand implements Hologram {

    private static final AABB EMPTY_BOUND = new AABB(0D, 0D, 0D, 0D, 0D, 0D);

    private CraftEntity bukkitEntity;

    public EntityHologram(ServerLevel serverLevel, double x, double y, double z) {
        super(serverLevel, x, y, z);

        setInvisible(true);
        setSmall(true);
        setShowArms(false);
        setNoGravity(true);
        setNoBasePlate(true);
        setMarker(true);

        super.collides = false;
        super.setCustomNameVisible(true); // Custom name visible
        super.setBoundingBox(EMPTY_BOUND);
    }

    @Override
    public void setHologramName(String name) {
        super.setCustomName(CraftChatMessage.fromStringOrNull(name));
    }

    @Override
    public void removeHologram() {
        if (Scheduler.isRegionScheduler() || !Bukkit.isPrimaryThread()) {
            World world = level().getWorld();
            int chunkX = getBlockX() >> 4;
            int chunkZ = getBlockZ() >> 4;
            Scheduler.runTask(world, chunkX, chunkZ, () -> super.remove(RemovalReason.DISCARDED));
        } else {
            super.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    public org.bukkit.entity.Entity getEntity() {
        return getBukkitEntity();
    }

    @Override
    public void tick() {
        // Disable normal ticking for this entity.

        // Workaround to force EntityTrackerEntry to send a teleport packet immediately after spawning this entity.
        if (this.onGround) {
            this.onGround = false;
        }
    }

    @Override
    public void inactiveTick() {
        // Disable normal ticking for this entity.

        // Workaround to force EntityTrackerEntry to send a teleport packet immediately after spawning this entity.
        if (this.onGround) {
            this.onGround = false;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        // Do not save NBT.
    }

    @Override
    public boolean saveAsPassenger(CompoundTag compoundTag) {
        // Do not save NBT.
        return false;
    }

    @Override
    public CompoundTag saveWithoutId(CompoundTag compoundTag) {
        // Do not save NBT.
        return compoundTag;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        // Do not load NBT.
    }

    @Override
    public void load(CompoundTag compoundTag) {
        // Do not load NBT.
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel serverLevel, DamageSource source) {
        /*
         * The field Entity.invulnerable is private.
         * It's only used while saving NBTTags, but since the entity would be killed
         * on chunk unload, we prefer to override isInvulnerable().
         */
        return true;
    }

    @Override
    public boolean repositionEntityAfterLoad() {
        return false;
    }

    @Override
    public void setCustomName(Component component) {
        // Locks the custom name.
    }

    @Override
    public void setCustomNameVisible(boolean visible) {
        // Locks the custom name.
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 hitPos, InteractionHand hand) {
        // Prevent stand being equipped
        return InteractionResult.PASS;
    }

    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack, boolean silent) {
        // Prevent stand being equipped
    }

    @Override
    public void playSound(SoundEvent soundEvent, float volume, float pitch) {
        // Remove sounds.
    }

    @Override
    public void remove(RemovalReason removalReason) {
        // Prevent being killed.
    }

    @Override
    public CraftEntity getBukkitEntity() {
        if (bukkitEntity == null) {
            bukkitEntity = new CraftArmorStand((CraftServer) Bukkit.getServer(), this);
        }
        return bukkitEntity;
    }

}
