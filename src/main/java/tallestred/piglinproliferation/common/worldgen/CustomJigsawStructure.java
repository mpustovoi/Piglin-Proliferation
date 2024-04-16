package tallestred.piglinproliferation.common.worldgen;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.slf4j.Logger;

import java.util.*;

/**
 * Copied from vanilla {@link net.minecraft.world.level.levelgen.structure.structures.JigsawStructure}
 */
public class CustomJigsawStructure extends Structure {
    public static final Codec<CustomJigsawStructure> CODEC = ExtraCodecs.validate(RecordCodecBuilder.mapCodec((kind) -> {
        return kind.group(settingsCodec(kind), StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter((structure) -> {
            return structure.startPool;
        }), ResourceLocation.CODEC.optionalFieldOf("start_jigsaw_name").forGetter((structure) -> {
            return structure.startJigsawName;
        }), Codec.intRange(0, 7).fieldOf("size").forGetter((structure) -> {
            return structure.maxDepth;
        }), HeightProvider.CODEC.fieldOf("start_height").forGetter((structure) -> {
            return structure.startHeight;
        }), Codec.BOOL.fieldOf("use_expansion_hack").forGetter((structure) -> {
            return structure.useExpansionHack;
        }), Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter((structure) -> {
            return structure.projectStartToHeightmap;
        }), Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter((structure) -> {
            return structure.maxDistanceFromCenter;
        }), Codec.list(PoolAliasBinding.CODEC).optionalFieldOf("pool_aliases", List.of()).forGetter(p_307187_ -> p_307187_.poolAliases)).apply(kind, CustomJigsawStructure::new);
    }), CustomJigsawStructure::verifyRange).codec();
    static final Logger LOGGER = LogUtils.getLogger();
    private final Holder<StructureTemplatePool> startPool;
    private final Optional<ResourceLocation> startJigsawName;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    private final List<PoolAliasBinding> poolAliases;


    private static DataResult<CustomJigsawStructure> verifyRange(CustomJigsawStructure structure) {
        byte b0 = switch (structure.terrainAdaptation()) {
            case NONE -> 0;
            case BURY, BEARD_THIN, BEARD_BOX -> 12;
        };
        return structure.maxDistanceFromCenter + b0 > 128 ? DataResult.error(() -> "Structure size including terrain adaptation must not exceed 128") : DataResult.success(structure);
    }

    public CustomJigsawStructure(Structure.StructureSettings structureSettings, Holder<StructureTemplatePool> startPool, Optional<ResourceLocation> startJigsawName, int maxDepth, HeightProvider startHeight, boolean useExpansionHack, Optional<Heightmap.Types> projectStartToHeightmap, int maxDistanceFromCenter, List<PoolAliasBinding> poolAliases) {
        super(structureSettings);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.maxDepth = maxDepth;
        this.startHeight = startHeight;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.poolAliases = poolAliases;
    }

    public CustomJigsawStructure(Structure.StructureSettings structureSettings, Holder<StructureTemplatePool> startPool, int maxDepth, HeightProvider startHeight, boolean useExpansionHack, Heightmap.Types projectStartToHeightmap) {
        this(structureSettings, startPool, Optional.empty(), maxDepth, startHeight, useExpansionHack, Optional.of(projectStartToHeightmap), 80, List.of());
    }

    public CustomJigsawStructure(Structure.StructureSettings pSettings, Holder<StructureTemplatePool> startPool, int maxDepth, HeightProvider startHeight, boolean useExpansionHack) {
        this(pSettings, startPool, Optional.empty(), maxDepth, startHeight, useExpansionHack, Optional.empty(), 80,  List.of());
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunkpos = context.chunkPos();
        BlockPos highLand = PPWorldgen.getHighestLand(context.chunkGenerator(), context.randomState(), new BoundingBox(new BlockPos(chunkpos.getMinBlockX(), this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor())), chunkpos.getMinBlockZ())), context.heightAccessor());
        if (highLand == null)
            return Optional.empty();
        BlockPos blockPos = new BlockPos(chunkpos.getMinBlockX(), highLand.getY(), chunkpos.getMinBlockZ());
        PoolAliasLookup lookup = PoolAliasLookup.create(this.poolAliases, blockPos, context.seed());
        StructureTemplatePool structuretemplatepool = this.startPool.unwrapKey().flatMap((key) -> context.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL).getOptional(lookup.lookup(key))).orElse(this.startPool.value());
        for (StructurePoolElement template : structuretemplatepool.templates)
            if (template instanceof ExclusiveListPoolElement element)
                element.addContext(context, blockPos); //Annoying hack
        return JigsawPlacement.addPieces(context, this.startPool, this.startJigsawName, this.maxDepth, blockPos, this.useExpansionHack, this.projectStartToHeightmap, this.maxDistanceFromCenter, lookup);
    }

    public StructureType<?> type() {
        return PPWorldgen.CUSTOM_JIGSAW.get();
    }
}