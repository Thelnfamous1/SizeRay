package me.Thelnfamous1.size_ray;

import me.Thelnfamous1.size_ray.capability.SizeRayUserCapability;
import me.Thelnfamous1.size_ray.capability.SizeRayUserInterface;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.Optional;

public class SizeRayGun extends Item {

    public static final float SCALE_STEP = 1 / 20.0F;

    public SizeRayGun(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pPlayer.startUsingItem(pHand);
        pLevel.playSound(pPlayer, pPlayer.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.0F);
        if(!pLevel.isClientSide){
            EnergyBeam energyBeam = new EnergyBeam(pLevel, pPlayer);
            if(pPlayer.isSecondaryUseActive()){
                setGrowing(energyBeam);
            } else{
                setShrinking(energyBeam);
            }
            pLevel.addFreshEntity(energyBeam);
            SizeRayUserCapability.getOptional(pPlayer).ifPresent(cap -> cap.setSizeRayBeamId(energyBeam.getId()));
        }
        return InteractionResultHolder.consume(itemstack);
    }

    private static void setShrinking(EnergyBeam energyBeam) {
        energyBeam.setColor(DyeColor.RED.getTextColor());
    }

    private static void setGrowing(EnergyBeam energyBeam) {
        energyBeam.setColor(DyeColor.LIGHT_BLUE.getTextColor());
    }

    @Override
    public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
        if(!pLevel.isClientSide){
            Optional<EntityHitResult> entityHitResult = SizeRayUtil.getEntityHitResult(pLivingEntity, SizeRayUtil.MAX_HIT_DISTANCE);
            entityHitResult.ifPresent(ehr -> {
                Entity target = ehr.getEntity();
                ScaleData scaleData = ScaleTypes.BASE.getScaleData(target);
                float scaleAddition = SCALE_STEP;
                SizeRayUserInterface sizeRayUser = SizeRayUserCapability.getCapability(pLivingEntity);
                if(sizeRayUser.getSizeRayBeamId() != 0){
                    EnergyBeam energyBeam = SizeRayUtil.getEnergyBeam(pLevel, sizeRayUser.getSizeRayBeamId());
                    if(energyBeam != null){
                        if(pLivingEntity.isShiftKeyDown()){ // secondary use
                            //SizeRayMod.LOGGER.info("{} is growing", target);
                            setGrowing(energyBeam);
                        } else{
                            //SizeRayMod.LOGGER.info("{} is shrinking", target);
                            scaleAddition *= -1;
                            setShrinking(energyBeam);
                        }
                    }
                }
                float baseScale = scaleData.getBaseScale();
                //SizeRayMod.LOGGER.info("Resizing {} from {} to {}", target, baseScale, baseScale + scaleAddition);
                scaleData.setScale(baseScale + scaleAddition);
            });
        }
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        pLevel.playSound(pLivingEntity instanceof Player player ? player : null, pLivingEntity.blockPosition(),
                SoundEvents.BEACON_DEACTIVATE, pLivingEntity instanceof Player ? SoundSource.PLAYERS : SoundSource.NEUTRAL, 1.0F, 1.0F);
        if(!pLevel.isClientSide){
            SizeRayUserCapability.getOptional(pLivingEntity).ifPresent(cap -> {
                if(cap.getSizeRayBeamId() != 0){
                    EnergyBeam energyBeam = SizeRayUtil.getEnergyBeam(pLevel, cap.getSizeRayBeamId());
                    if(energyBeam != null){
                        energyBeam.discard();
                    }
                    cap.setSizeRayBeamId(0);
                }
            });
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }
}
