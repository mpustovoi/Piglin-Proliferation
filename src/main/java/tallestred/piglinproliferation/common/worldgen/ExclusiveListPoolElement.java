package tallestred.piglinproliferation.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
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
        return instance.group(ElementWithConditions.CODEC.listOf().fieldOf("elements").forGetter((exclusiveListPoolElement) -> {
            return exclusiveListPoolElement.elementsWithConditions;
        }), projectionCodec()).apply(instance, ExclusiveListPoolElement::new);
    });

    public final List<ElementWithConditions> elementsWithConditions;
    private static Structure.GenerationContext CACHED_GENERATION_CONTEXT; //Lord have mercy
    private static BlockPos CACHED_BLOCK_POS; //This is so unstable it's not even funny


    public ExclusiveListPoolElement(List<ElementWithConditions> elementsWithConditions, StructureTemplatePool.Projection projection) {
        super(elementsWithConditions.stream().map(ElementWithConditions::element).toList(), projection);
        this.elementsWithConditions = elementsWithConditions;
    }

    @Override
    public boolean place(StructureTemplateManager templateManager, WorldGenLevel worldGenLevel, StructureManager manager, ChunkGenerator generator, BlockPos pos1, BlockPos pos2, Rotation rotation, BoundingBox box, RandomSource random, boolean bool) {
        CACHED_BLOCK_POS = pos1;
        StructurePoolElement element = this.selectElement(CACHED_BLOCK_POS);
        return element != null && element.place(templateManager, worldGenLevel, manager, generator, pos1, pos2, rotation, box, random, bool);
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager templateManager, BlockPos pos, Rotation rotation, RandomSource random) {
        StructurePoolElement element = this.selectElement(CACHED_BLOCK_POS); //TODO block pos is sometimes 0,0,0????
        return element != null ? element.getShuffledJigsawBlocks(templateManager, pos, rotation, random) : new ArrayList<>();
    }

    public void addContext(Structure.GenerationContext context, BlockPos blockPos) {
        if (!context.equals(CACHED_GENERATION_CONTEXT))
            CACHED_GENERATION_CONTEXT = context;
        if (!blockPos.equals(CACHED_BLOCK_POS))
            CACHED_BLOCK_POS = blockPos;
    }

    //TODO make static if this works
    public StructurePoolElement selectElement(BlockPos pos) {
        if (CACHED_GENERATION_CONTEXT != null && pos != null) {
            for (ElementWithConditions entry : elementsWithConditions) {
                if (entry.conditions().isEmpty() || entry.conditions().stream().allMatch(c -> c.test(CACHED_GENERATION_CONTEXT, pos)))
                    return entry.element;
            }
        } else System.out.println("Piglin Proliferation: Generation context variable didn't work!!");
        return null;
    }

    public record ElementWithConditions(StructurePoolElement element, List<ListPoolElementCondition> conditions) {
        public static Codec<ElementWithConditions> CODEC = RecordCodecBuilder.create(instance -> {
            return instance.group(
                    StructurePoolElement.CODEC.fieldOf("element").forGetter(e -> e.element),
                    PPWorldgen.LIST_POOL_ELEMENT_CODEC.listOf().optionalFieldOf("conditions").forGetter(e -> Optional.ofNullable(e.conditions))
            ).apply(instance, (element, optionalConditions) -> new ElementWithConditions(element, optionalConditions.orElseGet(ArrayList::new)));
        });
    }
}
