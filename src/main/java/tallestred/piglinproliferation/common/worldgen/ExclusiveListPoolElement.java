package tallestred.piglinproliferation.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.ListPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.*;

public class ExclusiveListPoolElement extends ListPoolElement {
    public static final Codec<ExclusiveListPoolElement> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(
                ElementWithConditions.CODEC.listOf().fieldOf("elements").forGetter((exclusiveListPoolElement) -> {
            return exclusiveListPoolElement.elementsWithConditions;
        }), projectionCodec()).apply(instance, ExclusiveListPoolElement::new);
    });

    public final List<ElementWithConditions> elementsWithConditions;

    public ExclusiveListPoolElement(List<ElementWithConditions> elementsWithConditions, StructureTemplatePool.Projection projection) {
        super(elementsWithConditions.stream().map(ElementWithConditions::element).toList(), projection);
        this.elementsWithConditions = elementsWithConditions;
    }

    @Override
    public boolean place(StructureTemplateManager templateManager, WorldGenLevel worldGenLevel, StructureManager manager, ChunkGenerator generator, BlockPos pos1, BlockPos pos2, Rotation rotation, BoundingBox box, RandomSource random, boolean bool) {
        Structure.GenerationContext context = new Structure.GenerationContext(worldGenLevel.registryAccess(), generator, generator.getBiomeSource(), worldGenLevel.getLevel().getChunkSource().randomState(), templateManager, worldGenLevel.getSeed(), new ChunkPos(pos1), LevelHeightAccessor.create(worldGenLevel.getMinBuildHeight(), worldGenLevel.getHeight()), b -> true);
        StructurePoolElement element = this.selectElement(context, pos1);
        return element != null && element.place(templateManager, worldGenLevel, manager, generator, pos1, pos2, rotation, box, random, bool);
    }


    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocksCustom(StructureTemplateManager templateManager, BlockPos pos, Rotation rotation, RandomSource random, Structure.GenerationContext context) {
        StructurePoolElement element = this.selectElement(context, pos);
        return element != null ? element.getShuffledJigsawBlocks(templateManager, pos, rotation, random) : new ArrayList<>();
    }

    public StructurePoolElement selectElement(Structure.GenerationContext context, BlockPos pos) {
        for (ElementWithConditions entry : elementsWithConditions) {
            if (entry.conditions().isEmpty() || entry.conditions().stream().allMatch(c -> c.test(context, pos)))
                return entry.element;
        }
        return null;
    }

    public record ElementWithConditions(StructurePoolElement element, List<ListPoolElementCondition> conditions) {
        public static Codec<ElementWithConditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                StructurePoolElement.CODEC.fieldOf("element").forGetter(e -> e.element),
                PPWorldgen.LIST_POOL_ELEMENT_CODEC.listOf().optionalFieldOf("conditions").forGetter(e -> Optional.ofNullable(e.conditions))
        ).apply(instance, (element, optionalConditions) -> new ElementWithConditions(element, optionalConditions.orElseGet(ArrayList::new))));
    }
}
