package tallestred.piglinproliferation.common.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public abstract class ListPoolElementCondition {
    public abstract boolean test(StructureTemplateManager templateManager, WorldGenLevel worldGenLevel, StructureManager manager, ChunkGenerator generator, BlockPos pos1, BlockPos pos2, Rotation rotation, BoundingBox box, RandomSource random, boolean bool);

    public abstract Type type();

    public record Type(Codec<? extends ListPoolElementCondition> codec) {}
}
