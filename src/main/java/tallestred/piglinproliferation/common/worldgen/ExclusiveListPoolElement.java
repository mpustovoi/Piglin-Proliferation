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
    private StructurePoolElement selectedElement;

    public ExclusiveListPoolElement(List<ElementWithConditions> elementsWithConditions, StructureTemplatePool.Projection projection) {
        super(elementsWithConditions.stream().map(ElementWithConditions::element).toList(), projection);
        this.elementsWithConditions = elementsWithConditions;
    }

    @Override
    public boolean place(StructureTemplateManager templateManager, WorldGenLevel worldGenLevel, StructureManager manager, ChunkGenerator generator, BlockPos pos1, BlockPos pos2, Rotation rotation, BoundingBox box, RandomSource random, boolean bool) {
        return this.selectedElement != null && this.selectedElement.place(templateManager, worldGenLevel, manager, generator, pos1, pos2, rotation, box, random, bool);
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureTemplateManager templateManager, BlockPos pos, Rotation rotation, RandomSource random) {
        List<StructureTemplate.StructureBlockInfo> list = new ArrayList<>();
        if (selectedElement != null)
            list = selectedElement.getShuffledJigsawBlocks(templateManager, pos, rotation, random);
        return list;
    }

    public void setElement(Structure.GenerationContext context, BlockPos pos) {
        for (ElementWithConditions entry : elementsWithConditions) {
            if (entry.conditions().isEmpty() || entry.conditions().stream().allMatch(c -> c.test(context, pos)))
                selectedElement = entry.element;
        }
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
