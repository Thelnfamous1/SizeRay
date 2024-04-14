package me.Thelnfamous1.size_ray.capability;

import net.minecraft.nbt.CompoundTag;

public class SizeRayUserImplementation implements SizeRayUserInterface {
    private int sizeRayBeamId;

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
    }

    @Override
    public int getSizeRayBeamId() {
        return this.sizeRayBeamId;
    }

    @Override
    public void setSizeRayBeamId(int sizeRayBeamId) {
        this.sizeRayBeamId = sizeRayBeamId;
    }
}
