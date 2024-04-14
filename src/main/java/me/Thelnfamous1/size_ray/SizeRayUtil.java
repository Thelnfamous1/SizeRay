package me.Thelnfamous1.size_ray;

import com.mojang.datafixers.util.Either;
import me.Thelnfamous1.size_ray.capability.SizeRayUserCapability;
import me.Thelnfamous1.size_ray.capability.SizeRayUserInterface;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.entity.PartEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class SizeRayUtil {

    public static final int MAX_HIT_DISTANCE = 1024;

    public static Either<BlockHitResult, EntityHitResult> getBeamHitResult(Entity shooter, double maxHitDistance) {
        BlockHitResult hitResult = (BlockHitResult) shooter.pick(maxHitDistance, 1.0F, false);
        Vec3 startVec = shooter.getEyePosition();
        double maxHitDistanceSqr = maxHitDistance * maxHitDistance;
        if (hitResult.getType() != HitResult.Type.MISS) {
            maxHitDistanceSqr = hitResult.getLocation().distanceToSqr(startVec);
        }
        Vec3 viewVector = shooter.getViewVector(1.0F).scale(maxHitDistance);
        Vec3 endVec = startVec.add(viewVector);
        AABB searchBox = shooter.getBoundingBox().expandTowards(viewVector).inflate(1.0D);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(shooter, startVec, endVec, searchBox, e -> canBeHit(e, shooter), maxHitDistanceSqr);
        if(entityHitResult != null) return Either.right(entityHitResult);
        return Either.left(hitResult);
    }

    private static boolean canBeHit(Entity entity, Entity shooter) {
        if(entity.isSpectator()) return false;
        int sizeRayBeamId = SizeRayUserCapability.getOptional(shooter).map(SizeRayUserInterface::getSizeRayBeamId).orElse(0);
        if(sizeRayBeamId != 0){
            EnergyBeam energyBeam = getEnergyBeam(shooter.level, sizeRayBeamId);
            if(energyBeam == entity){
                return false;
            }
        }

        return !(entity instanceof PartEntity<?>);
    }

    public static Optional<EntityHitResult> getEntityHitResult(LivingEntity shooter, double maxLockOnDist){
        return getBeamHitResult(shooter, maxLockOnDist).right();
    }

    @Nullable
    static EnergyBeam getEnergyBeam(Level pLevel, int sizeRayBeamId) {
        return pLevel.getEntity(sizeRayBeamId) instanceof EnergyBeam entity ? entity : null;
    }
}
