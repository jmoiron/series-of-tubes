package net.jmoiron.chubes.common.blocks;

import java.util.function.BiFunction;

import javax.annotation.Nullable;

import net.jmoiron.chubes.common.ConnectorType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BundleBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public BundleBlock(Properties properties) {
        super(properties);
    }

    // pre-calculate the shape of every bundle orientation + connection type
    // so that we don't have to combine voxel shapes on the fly
    private static VoxelShape[] shapeCache = null;

    // the basic bundle shape is a 6x6x6 cube
    public static VoxelShape SHAPE_BUNDLE_BASE = Shapes.box(5, 5, 5, 10, 10, 10);

    // it's okay if these shapes overlap; they get unioned to form the end shape

    // when connecting to other bundles, the bundle's 6x6 volume extends outward to
    // the edges of the block. The coordinates are 0->16, with
    // X = west -> east, Y = down -> up, and Z = north -> south
    public static VoxelShape SHAPE_B_NORTH = Shapes.box(5, 5, 0, 10, 10, 10);
    public static VoxelShape SHAPE_B_SOUTH = Shapes.box(5, 5, 5, 10, 10, 16);
    public static VoxelShape SHAPE_B_WEST = Shapes.box(0, 5, 5, 10, 10, 10);
    public static VoxelShape SHAPE_B_EAST = Shapes.box(5, 5, 5, 16, 10, 10);
    public static VoxelShape SHAPE_B_DOWN = Shapes.box(5, 0, 5, 10, 10, 10);
    public static VoxelShape SHAPE_B_UP   = Shapes.box(5, 16, 5, 10, 10, 10);

    // connectors are "fatter" 8x8x2 (one block extra in each direction)
    // cuffs that surround the ends of the bundle that illustrate a connection
    // to a block
    public static VoxelShape SHAPE_C_NORTH = Shapes.box(4, 4, 0, 11, 11, 2);
    public static VoxelShape SHAPE_C_SOUTH = Shapes.box(4, 4, 14, 10, 10, 16);
    public static VoxelShape SHAPE_C_WEST = Shapes.box(0, 4, 4, 2, 11, 11);
    public static VoxelShape SHAPE_C_EAST = Shapes.box(14, 4, 4, 16, 11, 11);
    public static VoxelShape SHAPE_C_DOWN = Shapes.box(4, 0, 4, 11, 2, 11);
    public static VoxelShape SHAPE_C_UP   = Shapes.box(4, 16, 4, 11, 14, 11);

    // initShapeCache sets up an array of all possible shapes, based on the
    // orientation of connections to other bundles and to blocks. We can then
    // use the block state to pull out the pre-calculated shape from cache.
    private void initShapeCache() {
        if (shapeCache == null) {
            int len = ConnectorType.TYPES.length;
            int cacheSize = len*len*len*len*len*len; // 6 directions, type^6 configurations

            shapeCache = new VoxelShape[cacheSize];
            for (ConnectorType north : ConnectorType.TYPES) {
                for (ConnectorType south : ConnectorType.TYPES) {
                    for (ConnectorType west : ConnectorType.TYPES) {
                        for (ConnectorType east : ConnectorType.TYPES) {
                            for (ConnectorType down : ConnectorType.TYPES) {
                                for (ConnectorType up : ConnectorType.TYPES) {
                                    int index = cacheIndex(north, south, west, east, down, up);
                                    shapeCache[index] = genVoxelShape(north, south, west, east, down, up);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private int cacheIndex(ConnectorType north, ConnectorType south, ConnectorType west, ConnectorType east, ConnectorType down, ConnectorType up) {
        // NOTE: if this were calculated inside-out, the cache could be built off of
        // a simple incrementing index, which would be better for caches etc,
        // but we only build the cache once so it's probably fine.
        int len = ConnectorType.TYPES.length;
        return ((((north.ordinal() * len + south.ordinal()) * len + west.ordinal()) * len + east.ordinal()) * len + down.ordinal()) * len + up.ordinal();
    }

    private VoxelShape genVoxelShape(ConnectorType north, ConnectorType south, ConnectorType west, ConnectorType east, ConnectorType down, ConnectorType up) {
        VoxelShape base = SHAPE_BUNDLE_BASE;
        base = combineShape(base, north, SHAPE_B_NORTH, SHAPE_C_NORTH);
        base = combineShape(base, south, SHAPE_B_SOUTH, SHAPE_C_SOUTH);
        base = combineShape(base, west, SHAPE_B_WEST, SHAPE_C_WEST);
        base = combineShape(base, east, SHAPE_B_EAST, SHAPE_C_EAST);
        base = combineShape(base, down, SHAPE_B_DOWN, SHAPE_C_DOWN);
        base = combineShape(base, up, SHAPE_B_UP, SHAPE_C_UP);
        return base;
    }

    // combineShape combines
    private VoxelShape combineShape(VoxelShape shape, ConnectorType type, VoxelShape bundle, VoxelShape block) {
        BiFunction<VoxelShape, VoxelShape, VoxelShape> union = (a, b) -> { return Shapes.join(a, b, BooleanOp.OR); };
        if (type == ConnectorType.CABLE) {
            return union.apply(shape, bundle);
        } else if (type == ConnectorType.BLOCK) {
            return union.apply(shape, union.apply(bundle, block));
        } else {
            return shape;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // TODO: use the shape cache
        return SHAPE_BUNDLE_BASE;
    }

    // needed or else invisible?
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return super.getRenderShape(state);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos arg0, BlockState arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'newBlockEntity'");
    }
}
