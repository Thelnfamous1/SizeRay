package me.Thelnfamous1.size_ray.capability;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class SizeRayUserCapability {

    public static final Capability<SizeRayUserInterface> INSTANCE = CapabilityManager.get(new CapabilityToken<>() {});

    public static SizeRayUserInterface getCapability(ICapabilityProvider entity){
        return getOptional(entity).orElseThrow(() -> new IllegalStateException("Missing Resize Capability!"));
    }

    public static LazyOptional<SizeRayUserInterface> getOptional(ICapabilityProvider entity){
        return entity.getCapability(INSTANCE);
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(SizeRayUserInterface.class);
    }

    public static void clone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        original.reviveCaps();
        getOptional(original)
                .ifPresent(oldCap -> getOptional(event.getEntity())
                        .ifPresent(newCap -> newCap.deserializeNBT(oldCap.serializeNBT())));
        original.invalidateCaps();
    }

    private SizeRayUserCapability() {
    }
}