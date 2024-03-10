package tallestred.piglinproliferation;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import tallestred.piglinproliferation.client.PPSounds;
import tallestred.piglinproliferation.common.enchantments.PPEnchantments;
import tallestred.piglinproliferation.common.entities.PiglinTraveller;
import tallestred.piglinproliferation.common.items.BucklerItem;
import tallestred.piglinproliferation.common.items.PPItems;
import tallestred.piglinproliferation.common.blockentities.PPBlockEntities;
import tallestred.piglinproliferation.common.blocks.PPBlocks;
import tallestred.piglinproliferation.common.entities.PPEntityTypes;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;
import tallestred.piglinproliferation.common.items.TravellersCompassItem;
import tallestred.piglinproliferation.common.loot.PPLoot;
import tallestred.piglinproliferation.common.recipes.PPRecipeSerializers;
import tallestred.piglinproliferation.configuration.PPConfig;
import tallestred.piglinproliferation.networking.PPNetworking;

import java.util.ArrayList;
import java.util.List;

@Mod(PiglinProliferation.MODID)
public class PiglinProliferation {
    public static final String MODID = "piglinproliferation";
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(
            Registries.PROCESSOR_LIST, new ResourceLocation("minecraft", "empty"));

    public PiglinProliferation() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addAttributes);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addSpawn);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::addCreativeTabs);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::serverStart);
        PPSounds.SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        PPItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        PPEntityTypes.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        PPMemoryModules.MEMORY_MODULE_TYPE.register(FMLJavaModLoadingContext.get().getModEventBus());
        PPActivities.ACTIVITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        PPBlockEntities.BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        PPBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        PPEnchantments.ENCHANTMENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        PPLoot.GLM.register(FMLJavaModLoadingContext.get().getModEventBus());
        PPLoot.LOOT_ITEM_FUNCTION_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        PPLoot.LOOT_ITEM_CONDITION_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        PPRecipeSerializers.RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, PPConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, PPConfig.CLIENT_SPEC);
        PPNetworking.registerPackets();
    }

    /**
     * Adds the building to the targeted pool.
     * <p>
     * used from https://gist.github.com/TelepathicGrunt/4fdbc445ebcbcbeb43ac748f4b18f342
     * Note: This is an additive operation which means multiple mods can do this and they stack with each other safely.
     */
    public static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry,
                                         Registry<StructureProcessorList> processorListRegistry,
                                         ResourceLocation poolRL,
                                         String nbtPieceRL,
                                         int weight) {

        // Grabs the processor list we want to use along with our piece.
        // This is a requirement as using the ProcessorLists.EMPTY field will cause the game to throw errors.
        // The reason why is the empty processor list in the world's registry is not the same instance as in that field once the world is started up.
        Holder<StructureProcessorList> emptyProcessorList = processorListRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);

        // Grab the pool we want to add to
        StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
        if (pool == null) return;

        // Grabs the nbt piece and creates a SinglePoolElement of it that we can add to a structure's pool.
        SinglePoolElement piece = SinglePoolElement.single(nbtPieceRL, emptyProcessorList).apply(StructureTemplatePool.Projection.RIGID);

        // Use AccessTransformer or Accessor Mixin to make StructureTemplatePool's templates field public for us to see.
        // Weight is handled by how many times the entry appears in this list.
        // We do not need to worry about immutability as this field is created using Lists.newArrayList(); which makes a mutable list.
        for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
        }

        // Use AccessTransformer or Accessor Mixin to make StructureTemplatePool's rawTemplates field public for us to see.
        // This list of pairs of pieces and weights is not used by vanilla by default but another mod may need it for efficiency.
        // So lets add to this list for completeness. We need to make a copy of the array as it can be an immutable list.
        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
        listOfPieceEntries.add(new Pair<>(piece, weight));
        pool.rawTemplates = listOfPieceEntries;
    }

    private void addAttributes(final EntityAttributeCreationEvent event) {
        event.put(PPEntityTypes.PIGLIN_TRAVELLER.get(), PiglinAlchemist.createAttributes().build());
        event.put(PPEntityTypes.PIGLIN_ALCHEMIST.get(), PiglinAlchemist.createAttributes().build());
    }

    private void addCreativeTabs(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(PPItems.PIGLIN_ALCHEMIST_SPAWN_EGG.get());
            event.accept(PPItems.PIGLIN_TRAVELLER_SPAWN_EGG.get());
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(PPItems.PIGLIN_ALCHEMIST_HEAD_ITEM.get());
            event.accept(PPItems.PIGLIN_BRUTE_HEAD_ITEM.get());
            event.accept(PPItems.ZOMBIFIED_PIGLIN_HEAD_ITEM.get());
           event.accept(PPItems.PIGLIN_TRAVELLER_HEAD_ITEM.get());
        }
        if (event.getTabKey() == CreativeModeTabs.COMBAT)
            event.accept(PPItems.BUCKLER.get());
    }

    private void addSpawn(final SpawnPlacementRegisterEvent event) {
        event.register(PPEntityTypes.PIGLIN_ALCHEMIST.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PiglinAlchemist::checkChemistSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
        event.register(PPEntityTypes.PIGLIN_TRAVELLER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, PiglinTraveller::checkTravellerSpawnRules, SpawnPlacementRegisterEvent.Operation.AND);
    }

    private void setup(final FMLCommonSetupEvent event) {
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
    }

    private void processIMC(final InterModProcessEvent event) {
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(new ItemModelHandler());
    }

    public static class ItemModelHandler {
        public ItemModelHandler() {
            ItemProperties.register(PPItems.BUCKLER.get(), new ResourceLocation("blocking"),
                    (stack, clientWorld, livingEntity, useTime) -> {
                        boolean active = livingEntity != null && livingEntity.isUsingItem()
                                && livingEntity.getUseItem() == stack
                                || livingEntity != null && BucklerItem.isReady(stack);
                        return livingEntity != null && active ? 1.0F : 0.0F;
                    });
            ItemProperties.register(PPItems.TRAVELLERS_COMPASS.get(), new ResourceLocation("angle"), new CompassItemPropertyFunction((level, itemStack, player) -> TravellersCompassItem.getPosition(itemStack.getOrCreateTag())));
        }
    }

    private void serverStart(final ServerAboutToStartEvent event) {
        Registry<StructureTemplatePool> templatePoolRegistry = event.getServer().registryAccess().registry(Registries.TEMPLATE_POOL).orElseThrow();
        Registry<StructureProcessorList> processorListRegistry = event.getServer().registryAccess().registry(Registries.PROCESSOR_LIST).orElseThrow();
        addBuildingToPool(templatePoolRegistry, processorListRegistry,
                new ResourceLocation("minecraft:bastion/mobs/piglin"),
                "piglinproliferation:bastion/alchemist_piglin", PPConfig.COMMON.alchemistWeightInBastions.get());
    }
}
