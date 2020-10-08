package com.bgsoftware.wildloaders.nms;

import com.bgsoftware.wildloaders.api.holograms.Hologram;
import net.minecraft.server.v1_8_R1.AxisAlignedBB;
import net.minecraft.server.v1_8_R1.DamageSource;
import net.minecraft.server.v1_8_R1.EntityArmorStand;
import net.minecraft.server.v1_8_R1.EntityHuman;
import net.minecraft.server.v1_8_R1.ItemStack;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import net.minecraft.server.v1_8_R1.Vec3D;
import net.minecraft.server.v1_8_R1.World;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

@SuppressWarnings("unused")
public final class EntityHolograms_v1_8_R1 extends EntityArmorStand implements Hologram {

    EntityHolograms_v1_8_R1(World world, double x, double y, double z){
        super(world, x, y, z);
        setInvisible(true);
        setSmall(true);
        setArms(false);
        setGravity(false);
        setBasePlate(true);
        super.setCustomNameVisible(true);
        super.a(new AxisAlignedBB(0D, 0D, 0D, 0D, 0D, 0D));
    }

    @Override
    public void setHologramName(String name) {
        super.setCustomName(name);
    }

    @Override
    public void removeHologram() {
        super.die();
    }

    @Override
    public Entity getEntity() {
        return getBukkitEntity();
    }

    @Override
    public void s_() {
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
    public void b(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
    }

    @Override
    public boolean c(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
        return false;
    }

    @Override
    public boolean d(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
        return false;
    }

    @Override
    public void e(NBTTagCompound nbttagcompound) {
        // Do not save NBT.
    }


    @Override
    public boolean isInvulnerable(DamageSource source) {
        /*
         * The field Entity.invulnerable is private.
         * It's only used while saving NBTTags, but since the entity would be killed
         * on chunk unload, we prefer to override isInvulnerable().
         */
        return true;
    }

    @Override
    public void setCustomName(String customName) {
        // Locks the custom name.
    }

    @Override
    public void setCustomNameVisible(boolean visible) {
        // Locks the custom name.
    }

    @Override
    public boolean a(EntityHuman human, Vec3D vec3d) {
        // Prevent stand being equipped
        return true;
    }

    @Override
    public boolean d(int i, ItemStack item) {
        // Prevent stand being equipped
        return false;
    }

    @Override
    public void setEquipment(int i, ItemStack item) {
        // Prevent stand being equipped
    }

    @Override
    public void a(AxisAlignedBB boundingBox) {
        // Do not change it!
    }

    @Override
    public void makeSound(String sound, float f1, float f2) {
        // Remove sounds.
    }

    @Override
    public void die() {
        // Prevent being killed.
    }

    @Override
    public CraftEntity getBukkitEntity() {
        if (super.bukkitEntity == null) {
            this.bukkitEntity = new CraftArmorStand(this.world.getServer(), this);
        }
        return this.bukkitEntity;
    }

}
