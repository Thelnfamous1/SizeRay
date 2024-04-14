package me.Thelnfamous1.size_ray;

import me.Thelnfamous1.size_ray.network.C2SEnergyBeamPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class EnergyBeam extends Entity implements OwnableEntity {
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(EnergyBeam.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> BEAM_WIDTH = SynchedEntityData.defineId(EnergyBeam.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(EnergyBeam.class, EntityDataSerializers.OPTIONAL_UUID);

    public EnergyBeam(EntityType<?> type, Level world) {
        super(type, world);
    }

    public EnergyBeam(Level world, LivingEntity shooter) {
        this(SizeRayMod.ENERGY_BEAM.get(), world, shooter);
    }

    public EnergyBeam(EntityType<?> type, Level world, LivingEntity shooter) {
        super(type, world);
        this.setOwner(shooter);
        this.updatePositionAndRotation(shooter);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_COLOR, 16777215);
        this.entityData.define(BEAM_WIDTH, 0.5F);
        this.entityData.define(DATA_OWNER_UUID, Optional.empty());
    }

    @Override
    public void tick() {
        Entity owner = this.getOwner();
        if(!this.level.isClientSide) {
            if (owner == null || !owner.isAlive()) {
                this.discard();
                return;
            }
        }
        if (owner != null && this.level.isClientSide()){
            this.updatePositionAndRotation(owner);
            SizeRayMod.NETWORK.sendToServer(new C2SEnergyBeamPacket(this));
        } else if (owner != null && !this.level.isClientSide()) {
            this.updatePositionAndRotation(owner);
        }
    }

    public void updatePositionAndRotation(Entity owner) {
        Vec3 offset = owner.position().add(this.getOffsetVector(owner));
        this.setPos(offset.x, offset.y, offset.z);
        this.setYRot(this.boundDegrees(owner.getYRot()));
        this.setXRot(this.boundDegrees(owner.getXRot()));
        this.yRotO = this.boundDegrees(owner.yRotO);
        this.xRotO = this.boundDegrees(owner.xRotO);
    }

    private Vec3 getOffsetVector(Entity owner) {
        Vec3 viewVector = owner.getViewVector(1.0F).scale(owner.getBbWidth() * 0.5F + this.getBbWidth() * 0.5F);
        return new Vec3(viewVector.x, owner.getBbHeight() * 0.5F, viewVector.z);
    }

    private float boundDegrees(float degrees){
        return (degrees % 360 + 360) % 360;
    }

    public float getBeamWidth() {
        return this.entityData.get(BEAM_WIDTH);
    }

    public void setBeamWidth(float beamWidth){
        this.entityData.set(BEAM_WIDTH, beamWidth);
    }

    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public void setOwner(@Nullable LivingEntity owner){
        this.setOwnerUUID(owner == null ? null : owner.getUUID());
    }

    private void setOwnerUUID(@Nullable UUID ownerUUID) {
        this.entityData.set(DATA_OWNER_UUID, Optional.ofNullable(ownerUUID));
    }

    @Nullable
    @Override
    public LivingEntity getOwner() {
        UUID ownerUUID = this.getOwnerUUID();
        if(ownerUUID != null){
            return this.level.getPlayerByUUID(ownerUUID);
        }
        return null;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        if (pCompound.hasUUID("Owner")) {
            this.setOwnerUUID(pCompound.getUUID("Owner"));
        }

        if (pCompound.contains("Color", Tag.TAG_ANY_NUMERIC)) {
            this.setColor(pCompound.getInt("Color"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        UUID ownerUUID = this.getOwnerUUID();
        if(ownerUUID != null){
            pCompound.putUUID("Owner", ownerUUID);
        }
        pCompound.putInt("Color", this.getColor());
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
    }

    public int getColor() {
        return this.getEntityData().get(DATA_COLOR);
    }

    public void setColor(int pColor) {
        this.getEntityData().set(DATA_COLOR, pColor);
    }
}