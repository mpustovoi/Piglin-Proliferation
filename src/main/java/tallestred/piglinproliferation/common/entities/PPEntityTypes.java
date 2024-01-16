package tallestred.piglinproliferation.common.entities;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tallestred.piglinproliferation.PiglinProliferation;

@Mod.EventBusSubscriber(modid = PiglinProliferation.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PPEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, PiglinProliferation.MODID);
    public static final RegistryObject<EntityType<PiglinAlchemist>> PIGLIN_ALCHEMIST = ENTITIES.register("piglin_alchemist", () -> EntityType.Builder.of(PiglinAlchemist::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).setShouldReceiveVelocityUpdates(true).build(PiglinProliferation.MODID + "piglin_alchemist"));
    public static final RegistryObject<EntityType<PiglinTraveller>> PIGLIN_TRAVELLER = ENTITIES.register("piglin_traveller", () -> EntityType.Builder.of(PiglinTraveller::new, MobCategory.MONSTER).sized(0.6F, 1.95F).clientTrackingRange(8).setShouldReceiveVelocityUpdates(true).build(PiglinProliferation.MODID + "piglin_traveller"));
}
