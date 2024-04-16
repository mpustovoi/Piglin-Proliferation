package tallestred.piglinproliferation.common.worldgen;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SequencedPriorityIterator;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

//For fuck's sake
public class CustomJigsawPlacement {
    static final Logger LOGGER = LogUtils.getLogger();

    public static Optional<Structure.GenerationStub> addPieces(
            Structure.GenerationContext p_227239_,
            Holder<StructureTemplatePool> p_227240_,
            Optional<ResourceLocation> p_227241_,
            int p_227242_,
            BlockPos p_227243_,
            boolean p_227244_,
            Optional<Heightmap.Types> p_227245_,
            int p_227246_,
            PoolAliasLookup p_307522_
    ) {
        RegistryAccess registryaccess = p_227239_.registryAccess();
        ChunkGenerator chunkgenerator = p_227239_.chunkGenerator();
        StructureTemplateManager structuretemplatemanager = p_227239_.structureTemplateManager();
        LevelHeightAccessor levelheightaccessor = p_227239_.heightAccessor();
        WorldgenRandom worldgenrandom = p_227239_.random();
        Registry<StructureTemplatePool> registry = registryaccess.registryOrThrow(Registries.TEMPLATE_POOL);
        Rotation rotation = Rotation.getRandom(worldgenrandom);
        StructureTemplatePool structuretemplatepool = p_227240_.unwrapKey()
                .flatMap(p_314915_ -> registry.getOptional(p_307522_.lookup(p_314915_)))
                .orElse(p_227240_.value());
        StructurePoolElement structurepoolelement = structuretemplatepool.getRandomTemplate(worldgenrandom);
        if (structurepoolelement == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        } else {
            BlockPos blockpos;
            if (p_227241_.isPresent()) {
                ResourceLocation resourcelocation = p_227241_.get();
                Optional<BlockPos> optional = getRandomNamedJigsaw(
                        structurepoolelement, resourcelocation, p_227243_, rotation, structuretemplatemanager, worldgenrandom, p_227239_
                );
                if (optional.isEmpty()) {
                    LOGGER.error(
                            "No starting jigsaw {} found in start pool {}",
                            resourcelocation,
                            p_227240_.unwrapKey().map(p_248484_ -> p_248484_.location().toString()).orElse("<unregistered>")
                    );
                    return Optional.empty();
                }

                blockpos = optional.get();
            } else {
                blockpos = p_227243_;
            }

            Vec3i vec3i = blockpos.subtract(p_227243_);
            BlockPos blockpos1 = p_227243_.subtract(vec3i);
            PoolElementStructurePiece poolelementstructurepiece = new PoolElementStructurePiece(
                    structuretemplatemanager,
                    structurepoolelement,
                    blockpos1,
                    structurepoolelement.getGroundLevelDelta(),
                    rotation,
                    structurepoolelement.getBoundingBox(structuretemplatemanager, blockpos1, rotation)
            );
            BoundingBox boundingbox = poolelementstructurepiece.getBoundingBox();
            int i = (boundingbox.maxX() + boundingbox.minX()) / 2;
            int j = (boundingbox.maxZ() + boundingbox.minZ()) / 2;
            int k = p_227245_.map(types -> p_227243_.getY() + chunkgenerator.getFirstFreeHeight(i, j, types, levelheightaccessor, p_227239_.randomState())).orElseGet(blockpos1::getY);
            int l = boundingbox.minY() + poolelementstructurepiece.getGroundLevelDelta();
            poolelementstructurepiece.move(0, k - l, 0);
            int i1 = k + vec3i.getY();
            return Optional.of(
                    new Structure.GenerationStub(
                            new BlockPos(i, i1, j),
                            p_307184_ -> {
                                List<PoolElementStructurePiece> list = Lists.newArrayList();
                                list.add(poolelementstructurepiece);
                                if (p_227242_ > 0) {
                                    AABB aabb = new AABB(
                                            i - p_227246_,
                                            i1 - p_227246_,
                                            j - p_227246_,
                                            i + p_227246_ + 1,
                                            i1 + p_227246_ + 1,
                                            j + p_227246_ + 1
                                    );
                                    VoxelShape voxelshape = Shapes.join(Shapes.create(aabb), Shapes.create(AABB.of(boundingbox)), BooleanOp.ONLY_FIRST);
                                    addPieces(
                                            p_227239_.randomState(),
                                            p_227242_,
                                            p_227244_,
                                            chunkgenerator,
                                            structuretemplatemanager,
                                            levelheightaccessor,
                                            worldgenrandom,
                                            registry,
                                            poolelementstructurepiece,
                                            list,
                                            voxelshape,
                                            p_307522_,
                                            p_227239_
                                    );
                                    list.forEach(p_307184_::addPiece);
                                }
                            }
                    )
            );
        }
    }

    private static Optional<BlockPos> getRandomNamedJigsaw(StructurePoolElement element, ResourceLocation location, BlockPos pos, Rotation rotation, StructureTemplateManager manager, WorldgenRandom random, Structure.GenerationContext context) {
        List<StructureTemplate.StructureBlockInfo> list;
        if (element instanceof ExclusiveListPoolElement exclusiveListPoolElement)
            list = exclusiveListPoolElement.getShuffledJigsawBlocksCustom(manager, pos, rotation, random, context);
        else list = element.getShuffledJigsawBlocks(manager, pos, rotation, random);
        Optional<BlockPos> optional = Optional.empty();
        for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : list) {
            ResourceLocation resourcelocation = ResourceLocation.tryParse(
                    Objects.requireNonNull(structuretemplate$structureblockinfo.nbt(), () -> structuretemplate$structureblockinfo + " nbt was null")
                            .getString("name")
            );
            if (location.equals(resourcelocation)) {
                optional = Optional.of(structuretemplate$structureblockinfo.pos());
                break;
            }
        }

        return optional;
    }

    private static void addPieces(
            RandomState p_227211_,
            int p_227212_,
            boolean p_227213_,
            ChunkGenerator p_227214_,
            StructureTemplateManager p_227215_,
            LevelHeightAccessor p_227216_,
            RandomSource p_227217_,
            Registry<StructureTemplatePool> p_227218_,
            PoolElementStructurePiece p_227219_,
            List<PoolElementStructurePiece> p_227220_,
            VoxelShape p_227221_,
            PoolAliasLookup p_307210_,
            Structure.GenerationContext context
    ) {
        Placer jigsawplacement$placer = new Placer(p_227218_, p_227212_, p_227214_, p_227215_, p_227220_, p_227217_);
        jigsawplacement$placer.tryPlacingChildren(p_227219_, new MutableObject<>(p_227221_), 0, p_227213_, p_227216_, p_227211_, p_307210_, context);

        while(jigsawplacement$placer.placing.hasNext()) {
            PieceState jigsawplacement$piecestate = jigsawplacement$placer.placing.next();
            jigsawplacement$placer.tryPlacingChildren(
                    jigsawplacement$piecestate.piece, jigsawplacement$piecestate.free, jigsawplacement$piecestate.depth, p_227213_, p_227216_, p_227211_, p_307210_, context
            );
        }
    }

    record PieceState(PoolElementStructurePiece piece, MutableObject<VoxelShape> free, int depth) {
    }

    static final class Placer {
        private final Registry<StructureTemplatePool> pools;
        private final int maxDepth;
        private final ChunkGenerator chunkGenerator;
        private final StructureTemplateManager structureTemplateManager;
        private final List<? super PoolElementStructurePiece> pieces;
        private final RandomSource random;
        final SequencedPriorityIterator<PieceState> placing = new SequencedPriorityIterator<>();

        Placer(
                Registry<StructureTemplatePool> p_227258_,
                int p_227259_,
                ChunkGenerator p_227260_,
                StructureTemplateManager p_227261_,
                List<? super PoolElementStructurePiece> p_227262_,
                RandomSource p_227263_
        ) {
            this.pools = p_227258_;
            this.maxDepth = p_227259_;
            this.chunkGenerator = p_227260_;
            this.structureTemplateManager = p_227261_;
            this.pieces = p_227262_;
            this.random = p_227263_;
        }

        void tryPlacingChildren(PoolElementStructurePiece piece, MutableObject<VoxelShape> mutableShape, int alwaysZero, boolean useExpansionHack, LevelHeightAccessor heightAccessor, RandomState randomState, PoolAliasLookup lookup, Structure.GenerationContext context) {
            StructurePoolElement structurepoolelement = piece.getElement();
            BlockPos blockpos = piece.getPosition();
            Rotation rotation = piece.getRotation();
            StructureTemplatePool.Projection structuretemplatepool$projection = structurepoolelement.getProjection();
            boolean flag = structuretemplatepool$projection == StructureTemplatePool.Projection.RIGID;
            MutableObject<VoxelShape> mutableobject = new MutableObject<>();
            BoundingBox boundingbox = piece.getBoundingBox();
            int i = boundingbox.minY();
            List<StructureTemplate.StructureBlockInfo> shuffledBlocks;
            if (structurepoolelement instanceof ExclusiveListPoolElement exclusiveListPoolElement)
                shuffledBlocks = exclusiveListPoolElement.getShuffledJigsawBlocksCustom(structureTemplateManager, blockpos, rotation, random, context);
            else shuffledBlocks = structurepoolelement.getShuffledJigsawBlocks(structureTemplateManager, blockpos, rotation, random);
            label134:
            for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : shuffledBlocks) {
                Direction direction = JigsawBlock.getFrontFacing(structuretemplate$structureblockinfo.state());
                BlockPos blockpos1 = structuretemplate$structureblockinfo.pos();
                BlockPos blockpos2 = blockpos1.relative(direction);
                int j = blockpos1.getY() - i;
                int k = -1;
                ResourceKey<StructureTemplatePool> resourcekey = readPoolKey(structuretemplate$structureblockinfo, lookup);
                Optional<? extends Holder<StructureTemplatePool>> optional = this.pools.getHolder(resourcekey);
                if (optional.isEmpty()) {
                    LOGGER.warn("Empty or non-existent pool: {}", resourcekey.location());
                } else {
                    Holder<StructureTemplatePool> holder = optional.get();
                    if (holder.value().size() == 0 && !holder.is(Pools.EMPTY)) {
                        LOGGER.warn("Empty or non-existent pool: {}", resourcekey.location());
                    } else {
                        Holder<StructureTemplatePool> holder1 = holder.value().getFallback();
                        if (holder1.value().size() == 0 && !holder1.is(Pools.EMPTY)) {
                            LOGGER
                                    .warn(
                                            "Empty or non-existent fallback pool: {}",
                                            holder1.unwrapKey().map(p_255599_ -> p_255599_.location().toString()).orElse("<unregistered>")
                                    );
                        } else {
                            boolean flag1 = boundingbox.isInside(blockpos2);
                            MutableObject<VoxelShape> mutableobject1;
                            if (flag1) {
                                mutableobject1 = mutableobject;
                                if (mutableobject.getValue() == null) {
                                    mutableobject.setValue(Shapes.create(AABB.of(boundingbox)));
                                }
                            } else {
                                mutableobject1 = mutableShape;
                            }

                            List<StructurePoolElement> list = Lists.newArrayList();
                            if (alwaysZero != this.maxDepth) {
                                list.addAll(holder.value().getShuffledTemplates(this.random));
                            }

                            list.addAll(holder1.value().getShuffledTemplates(this.random));
                            int l = structuretemplate$structureblockinfo.nbt() != null
                                    ? structuretemplate$structureblockinfo.nbt().getInt("placement_priority")
                                    : 0;

                            for(StructurePoolElement structurepoolelement1 : list) {
                                if (structurepoolelement1 == EmptyPoolElement.INSTANCE) {
                                    break;
                                }

                                for(Rotation rotation1 : Rotation.getShuffled(this.random)) {
                                    List<StructureTemplate.StructureBlockInfo> list1;
                                    if (structurepoolelement instanceof ExclusiveListPoolElement exclusiveListPoolElement)
                                        list1 = exclusiveListPoolElement.getShuffledJigsawBlocksCustom(structureTemplateManager, blockpos1, rotation1, random, context);
                                    else list1 = structurepoolelement.getShuffledJigsawBlocks(structureTemplateManager, BlockPos.ZERO, rotation, random);
                                    BoundingBox boundingbox1 = structurepoolelement1.getBoundingBox(this.structureTemplateManager, BlockPos.ZERO, rotation1);
                                    int i1;
                                    if (useExpansionHack && boundingbox1.getYSpan() <= 16) {
                                        i1 = list1.stream()
                                                .mapToInt(
                                                        p_255598_ -> {
                                                            if (!boundingbox1.isInside(p_255598_.pos().relative(JigsawBlock.getFrontFacing(p_255598_.state())))) {
                                                                return 0;
                                                            } else {
                                                                ResourceKey<StructureTemplatePool> resourcekey1 = readPoolKey(p_255598_, lookup);
                                                                Optional<? extends Holder<StructureTemplatePool>> optional1 = this.pools.getHolder(resourcekey1);
                                                                Optional<Holder<StructureTemplatePool>> optional2 = optional1.map(
                                                                        p_255600_ -> p_255600_.value().getFallback()
                                                                );
                                                                int k3 = optional1.map(
                                                                                p_255596_ -> p_255596_.value().getMaxSize(this.structureTemplateManager)
                                                                        )
                                                                        .orElse(0);
                                                                int l3 = optional2.<Integer>map(
                                                                                p_255601_ -> p_255601_.value().getMaxSize(this.structureTemplateManager)
                                                                        )
                                                                        .orElse(0);
                                                                return Math.max(k3, l3);
                                                            }
                                                        }
                                                )
                                                .max()
                                                .orElse(0);
                                    } else {
                                        i1 = 0;
                                    }

                                    for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo1 : list1) {
                                        if (JigsawBlock.canAttach(structuretemplate$structureblockinfo, structuretemplate$structureblockinfo1)) {
                                            BlockPos blockpos3 = structuretemplate$structureblockinfo1.pos();
                                            BlockPos blockpos4 = blockpos2.subtract(blockpos3);
                                            BoundingBox boundingbox2 = structurepoolelement1.getBoundingBox(this.structureTemplateManager, blockpos4, rotation1);
                                            int j1 = boundingbox2.minY();
                                            StructureTemplatePool.Projection structuretemplatepool$projection1 = structurepoolelement1.getProjection();
                                            boolean flag2 = structuretemplatepool$projection1 == StructureTemplatePool.Projection.RIGID;
                                            int k1 = blockpos3.getY();
                                            int l1 = j - k1 + JigsawBlock.getFrontFacing(structuretemplate$structureblockinfo.state()).getStepY();
                                            int i2;
                                            if (flag && flag2) {
                                                i2 = i + l1;
                                            } else {
                                                if (k == -1) {
                                                    k = this.chunkGenerator
                                                            .getFirstFreeHeight(
                                                                    blockpos1.getX(), blockpos1.getZ(), Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState
                                                            );
                                                }

                                                i2 = k - k1;
                                            }

                                            int j2 = i2 - j1;
                                            BoundingBox boundingbox3 = boundingbox2.moved(0, j2, 0);
                                            BlockPos blockpos5 = blockpos4.offset(0, j2, 0);
                                            if (i1 > 0) {
                                                int k2 = Math.max(i1 + 1, boundingbox3.maxY() - boundingbox3.minY());
                                                boundingbox3.encapsulate(new BlockPos(boundingbox3.minX(), boundingbox3.minY() + k2, boundingbox3.minZ()));
                                            }

                                            if (!Shapes.joinIsNotEmpty(
                                                    mutableobject1.getValue(), Shapes.create(AABB.of(boundingbox3).deflate(0.25)), BooleanOp.ONLY_SECOND
                                            )) {
                                                mutableobject1.setValue(
                                                        Shapes.joinUnoptimized(
                                                                mutableobject1.getValue(), Shapes.create(AABB.of(boundingbox3)), BooleanOp.ONLY_FIRST
                                                        )
                                                );
                                                int j3 = piece.getGroundLevelDelta();
                                                int l2;
                                                if (flag2) {
                                                    l2 = j3 - l1;
                                                } else {
                                                    l2 = structurepoolelement1.getGroundLevelDelta();
                                                }

                                                PoolElementStructurePiece poolelementstructurepiece = new PoolElementStructurePiece(
                                                        this.structureTemplateManager, structurepoolelement1, blockpos5, l2, rotation1, boundingbox3
                                                );
                                                int i3;
                                                if (flag) {
                                                    i3 = i + j;
                                                } else if (flag2) {
                                                    i3 = i2 + k1;
                                                } else {
                                                    if (k == -1) {
                                                        k = this.chunkGenerator
                                                                .getFirstFreeHeight(
                                                                        blockpos1.getX(), blockpos1.getZ(), Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState
                                                                );
                                                    }

                                                    i3 = k + l1 / 2;
                                                }

                                                piece.addJunction(
                                                        new JigsawJunction(blockpos2.getX(), i3 - j + j3, blockpos2.getZ(), l1, structuretemplatepool$projection1)
                                                );
                                                poolelementstructurepiece.addJunction(
                                                        new JigsawJunction(blockpos1.getX(), i3 - k1 + l2, blockpos1.getZ(), -l1, structuretemplatepool$projection)
                                                );
                                                this.pieces.add(poolelementstructurepiece);
                                                if (alwaysZero + 1 <= this.maxDepth) {
                                                    PieceState jigsawplacement$piecestate = new PieceState(
                                                            poolelementstructurepiece, mutableobject1, alwaysZero + 1
                                                    );
                                                    this.placing.add(jigsawplacement$piecestate, l);
                                                }
                                                continue label134;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        private static ResourceKey<StructureTemplatePool> readPoolKey(StructureTemplate.StructureBlockInfo p_307327_, PoolAliasLookup p_307490_) {
            CompoundTag compoundtag = Objects.requireNonNull(p_307327_.nbt(), () -> p_307327_ + " nbt was null");
            ResourceKey<StructureTemplatePool> resourcekey = Pools.createKey(compoundtag.getString("pool"));
            return p_307490_.lookup(resourcekey);
        }
    }
}

