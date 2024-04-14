package tallestred.piglinproliferation.common.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class ValidBiomesCondition extends ListPoolElementCondition {
    public static Codec<ValidBiomesCondition> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(Biome.LIST_CODEC.fieldOf("biomes").forGetter(o -> o.biomes)).apply(instance, ValidBiomesCondition::new);
    });

    public final HolderSet<Biome> biomes;

    public ValidBiomesCondition(HolderSet<Biome> biomes) {
        this.biomes = biomes;
    }

    @Override
    public boolean test(StructureTemplateManager templateManager, WorldGenLevel worldGenLevel, StructureManager manager, ChunkGenerator generator, BlockPos pos1, BlockPos pos2, Rotation rotation, BoundingBox box, RandomSource random, boolean bool) {
        return biomes.contains(worldGenLevel.getBiome(pos1));
    }

    @Override
    public Type type() {
        return PPWorldgen.VALID_BIOMES_CONDITION;
    }
}
