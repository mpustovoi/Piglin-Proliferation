package tallestred.piglinproliferation.common.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public abstract class ListPoolElementCondition {
    public abstract boolean test(Structure.GenerationContext context, BlockPos pos);

    public abstract Type type();

    public record Type(Codec<? extends ListPoolElementCondition> codec) {}
}
