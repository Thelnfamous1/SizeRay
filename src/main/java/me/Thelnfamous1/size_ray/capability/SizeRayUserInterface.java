package me.Thelnfamous1.size_ray.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface SizeRayUserInterface extends INBTSerializable<CompoundTag> {

    int getSizeRayBeamId();

    void setSizeRayBeamId(int sizeRayBeamId);
}
