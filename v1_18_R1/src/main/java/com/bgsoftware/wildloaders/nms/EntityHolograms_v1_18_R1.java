package com.bgsoftware.wildloaders.nms;

import com.bgsoftware.wildloaders.api.holograms.Hologram;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftChatMessage;

@SuppressWarnings("unused")
public final class EntityHolograms_v1_18_R1 extends EntityArmorStand implements Hologram {

    private static final AxisAlignedBB EMPTY_BOUND = new AxisAlignedBB(0D, 0D, 0D, 0D, 0D, 0D);

    private CraftEntity bukkitEntity;

    public EntityHolograms_v1_18_R1(World world, double x, double y, double z){
        super(world, x, y, z);
        j(true); // Invisible
        a(true); // Small
        r(false); // Arms
        e(true); // No Gravity
        s(true); // Base Plate
        t(true); // Marker
        super.collides = false;
        super.n(true); // Custom name visible
        super.a(EMPTY_BOUND);
    }

    @Override
    public void setHologramName(String name) {
        super.a(CraftChatMessage.fromString(name)[0]);
    }

    @Override
    public void removeHologram() {
        super.a(RemovalReason.b);
    }

    @Override
    public org.bukkit.entity.Entity getEntity() {
        return getBukkitEntity();
    }

    @Override
    public void k() {
        // Disable normal ticking for this entity.

        // Workaround to force EntityTrackerEntry to send a teleport packet immediately after spawning this entity.
        if (this.z) {
            this.z = false;
        }
    }

    @Override
    public void inactiveTick() {
        // Disable normal ticking for this entity.

        // Workaround to force EntityTrackerEntry to send a teleport packet immediately after spawning this entity.
        if (this.z) {
            this.z = false;
        }
    }

    @Override
    public void b(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
    }

    @Override
    public boolean d(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
        return false;
    }

    @Override
    public NBTTagCompound f(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
        return nbttagcompound;
    }

    @Override
    public void a(NBTTagCompound nbttagcompound) {
        // Do not load NBT.
    }

    @Override
    public void g(NBTTagCompound nbttagcompound) {
        // Do not load NBT.
    }

    @Override
    public boolean b(DamageSource source) {
        /*
         * The field Entity.invulnerable is private.
         * It's only used while saving NBTTags, but since the entity would be killed
         * on chunk unload, we prefer to override isInvulnerable().
         */
        return true;
    }

    @Override
    public boolean bi() {
        return false;
    }

    @Override
    public void a(IChatBaseComponent ichatbasecomponent) {
        // Locks the custom name.
    }

    @Override
    public void n(boolean flag) {
        // Locks the custom name.
    }

    @Override
    public EnumInteractionResult a(EntityHuman human, Vec3D vec3d, EnumHand enumhand) {
        // Prevent stand being equipped
        return EnumInteractionResult.d;
    }

    @Override
    public void setItemSlot(EnumItemSlot enumitemslot, ItemStack itemstack, boolean flag) {
        // Prevent stand being equipped
    }

    @Override
    public AxisAlignedBB cx() {
        return EMPTY_BOUND;
    }

    public void forceSetBoundingBox(AxisAlignedBB boundingBox) {
        super.a(boundingBox);
    }

    @Override
    public void a(SoundEffect soundeffect, float f, float f1) {
        // Remove sounds.
    }

    @Override
    public void a(RemovalReason entity_removalreason) {
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
