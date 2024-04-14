package me.Thelnfamous1.size_ray;

import com.mojang.logging.LogUtils;
import me.Thelnfamous1.size_ray.capability.SizeRayUserAttacher;
import me.Thelnfamous1.size_ray.capability.SizeRayUserCapability;
import me.Thelnfamous1.size_ray.client.SizeRayModClient;
import me.Thelnfamous1.size_ray.network.C2SEnergyBeamPacket;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.Optional;

@Mod(SizeRayMod.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SizeRayMod {
    public static final String MODID = "size_ray";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> SIZE_RAY_GUN = ITEMS.register("size_ray_gun", () -> new SizeRayGun(new Item.Properties().tab(CreativeModeTab.TAB_COMBAT)));

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    public static final RegistryObject<EntityType<EnergyBeam>> ENERGY_BEAM = register("energy_beam",
            EntityType.Builder.<EnergyBeam>of(EnergyBeam::new, MobCategory.MISC)
                    .fireImmune()
                    .setShouldReceiveVelocityUpdates(false)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(6)
                    .updateInterval(2));

    private static <T extends Entity> RegistryObject<EntityType<T>> register(String pKey, EntityType.Builder<T> pBuilder) {
        return ENTITY_TYPES.register(pKey, () -> pBuilder.build(MODID + ":" + pKey));
    }

    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "network"),
            () -> "1.0",
            client -> client.equals("1.0"),
            server -> server.equals("1.0"));

    private static int NETWORK_INDEX = 0;

    public static final TagKey<EntityType<?>> IMMUNE_TO_SIZE_RAY = TagKey.create(Registry.ENTITY_TYPE_REGISTRY, location("immune_to_size_ray"));

    public SizeRayMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);

        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, SizeRayUserAttacher::attach);
        if(FMLEnvironment.dist == Dist.CLIENT){
            SizeRayModClient.initializeClient(modEventBus);
        }
    }

    public static ResourceLocation location(String path){
        return new ResourceLocation(MODID, path);
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            NETWORK.registerMessage(NETWORK_INDEX++, C2SEnergyBeamPacket.class, C2SEnergyBeamPacket::write, C2SEnergyBeamPacket::new, C2SEnergyBeamPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        });
    }

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        SizeRayUserCapability.register(event);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        generator.addProvider(event.includeClient(), new LanguageProvider(generator, MODID, "en_us") {
            @Override
            protected void addTranslations() {
                this.add(SIZE_RAY_GUN.get(), "Size Ray Gun");
                this.add(ENERGY_BEAM.get(), "Energy Beam");
                this.add(SizeRayModClient.OBSCURE_SCREEN_KEY_NAME, "Obscure Screen");
                this.add(SizeRayModClient.KEY_CATEGORY, "Size Ray");
            }
        });

        generator.addProvider(event.includeServer(), new EntityTypeTagsProvider(generator, MODID, event.getExistingFileHelper()){
            @Override
            protected void addTags() {
                this.tag(IMMUNE_TO_SIZE_RAY).add(ENERGY_BEAM.get());
            }
        });
    }
}
