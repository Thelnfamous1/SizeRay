package me.Thelnfamous1.size_ray.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.Thelnfamous1.size_ray.SizeRayMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import virtuoel.pehkui.api.ScaleTypes;

public class SizeRayModClient {

    public static final String OBSCURE_SCREEN_KEY_NAME = "key.%s.obscure_screen".formatted(SizeRayMod.MODID);
    public static final String KEY_CATEGORY = "key.%s".formatted(SizeRayMod.MODID);
    public static final KeyMapping OBSCURE_SCREEN = new KeyMapping(OBSCURE_SCREEN_KEY_NAME, InputConstants.KEY_Z, KEY_CATEGORY);
    private static boolean WAS_KEY_DOWN = false;

    public static void initializeClient(IEventBus modEventBus){
        modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
                event.registerEntityRenderer(SizeRayMod.ENERGY_BEAM.get(), EnergyBeamRenderer::new));
        modEventBus.addListener((RegisterKeyMappingsEvent event) -> event.register(OBSCURE_SCREEN));
    }

    public static void tickInput(LocalPlayer player){
        boolean isKeyDown = OBSCURE_SCREEN.isDown();
        if (WAS_KEY_DOWN && !isKeyDown) {
            // Release
        } else if (!WAS_KEY_DOWN && isKeyDown) {
            // First press
            Vec3 center = player.getPosition(0);
            float scale = ScaleTypes.BASE.getScaleData(player).getScale();
            int segments = (int) (24 * scale);
            double r = 2 * scale;
            for (int segmentStep = 0; segmentStep < segments; segmentStep++) {
                double degrees = segmentStep * 360.0D / segments;
                double x = r * Math.cos(Math.toRadians(degrees));
                double z = r * Math.sin(Math.toRadians(degrees));
                int ySteps = (int) (32 * scale);
                for (int yStep = 0; yStep < ySteps; yStep++) {
                    double y = yStep / (8.0D * scale);
                    player.level.addParticle(ParticleTypes.POOF, center.x + x, center.y + y, center.z + z, 0, 0, 0);
                }
            }
        } else if (WAS_KEY_DOWN) {
            // Held
        }
        WAS_KEY_DOWN = isKeyDown;

    }
}
