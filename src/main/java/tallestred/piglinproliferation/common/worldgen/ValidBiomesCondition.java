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
import net.minecraft.world.level.levelgen.structure.Structure;
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
    public boolean test(Structure.GenerationContext context, BlockPos pos) {
        return biomes.contains(context.biomeSource().getNoiseBiome(pos.getX(), pos.getY(), pos.getZ(), context.randomState().sampler()));
    }

    @Override
    public Type type() {
        return PPWorldgen.VALID_BIOMES_CONDITION;
    }
}
