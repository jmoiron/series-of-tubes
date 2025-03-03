package net.jmoiron.chubes.common.blocks;

import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.jmoiron.chubes.common.ConnectorType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;

public class ChubesCableBlock extends Block implements SimpleWaterloggedBlock {

    // CableBlocks is an enumeration of blocks that are considered "cables"
    // by chubes. It's just the cable and the connector.
    public static enum CableBlocks {
      CABLE,
      CONNECTOR
    };

    // BlockState properties for chubes cables
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // shapes for the cardinal directions of this cable; the model for
    // this part of the cable is called `cable_ext` and it's the part of
    // the cable that extends out from the node when it connects to an
    // adjacent cable/connector
    public static final VoxelShape SHAPE_CABLE_NORTH = Block.box(5, 5, 0, 11, 11, 5);
    public static final VoxelShape SHAPE_CABLE_SOUTH = Block.box(5, 5, 11, 11, 11, 16);
    public static final VoxelShape SHAPE_CABLE_WEST = Block.box(0, 5, 5, 5, 11, 11);
    public static final VoxelShape SHAPE_CABLE_EAST = Block.box(11, 5, 5, 16, 11, 11);
    public static final VoxelShape SHAPE_CABLE_DOWN = Block.box(5, 0, 5, 11, 5, 11);
    public static final VoxelShape SHAPE_CABLE_UP = Block.box(5, 11, 5, 11, 16, 11);
    // the "node" of the cable is a centered 6x6x6 cube; it is the
    // default shape of a cable when it's placed next to non-cables
    public static final VoxelShape SHAPE_CABLE_NODE = Block.box(5, 5, 5, 11, 11, 11);

    public ChubesCableBlock(Block.Properties properties) {
        super(Properties.of()
                .noOcclusion()
                .forceSolidOn()
                .strength(1.0f)
        );

        // at construction, a cable is not connected to anything and not waterlogged
        registerDefaultState(
            stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(EAST, false)
                .setValue(DOWN, false)
                .setValue(UP, false)
                .setValue(WATERLOGGED, false)
        );
    }

    // the blockstate for this class has to be defined in order for
    // us to be able to set values for it, the class machinery does
    // not use some kind of introspection to auto-generate the blockstate
    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, SOUTH, WEST, EAST, DOWN, UP, WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // TODO: pre-cache voxelshapes so we don't have to re-calculate all the time.

        VoxelShape shape = SHAPE_CABLE_NODE;

        if (state.getValue(NORTH)) {
            shape = Shapes.join(shape, SHAPE_CABLE_NORTH, BooleanOp.OR);
        }
        if (state.getValue(SOUTH)) {
            shape = Shapes.join(shape, SHAPE_CABLE_SOUTH, BooleanOp.OR);
        }
        if (state.getValue(WEST)) {
            shape = Shapes.join(shape, SHAPE_CABLE_WEST, BooleanOp.OR);
        }
        if (state.getValue(EAST)) {
            shape = Shapes.join(shape, SHAPE_CABLE_EAST, BooleanOp.OR);
        }
        if (state.getValue(UP)) {
            shape = Shapes.join(shape, SHAPE_CABLE_UP, BooleanOp.OR);
        }
        if (state.getValue(DOWN)) {
            shape = Shapes.join(shape, SHAPE_CABLE_DOWN, BooleanOp.OR);
        }

        return shape;
    }

    /*
    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return SHAPE_CABLE_NODE;
    }
    */

    @Nonnull
    @Override
    public BlockState updateShape(BlockState state, @Nonnull Direction direction, @Nonnull BlockState otherState,
                                  @Nonnull LevelAccessor world, @Nonnull BlockPos current, @Nonnull BlockPos offset) {
        return getConnectorState(world, current, state);
    }
    // allow the block to drop itself when mined
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return super.getDrops(state, builder);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return super.getRenderShape(state);
    }

    public BlockState getConnectorState(LevelAccessor world, BlockPos pos, BlockState state) {
        return state
            .setValue(NORTH, shouldConnect(world, pos, Direction.NORTH))
            .setValue(SOUTH, shouldConnect(world, pos, Direction.SOUTH))
            .setValue(WEST, shouldConnect(world, pos, Direction.WEST))
            .setValue(EAST, shouldConnect(world, pos, Direction.EAST))
            .setValue(UP, shouldConnect(world, pos, Direction.UP))
            .setValue(DOWN, shouldConnect(world, pos, Direction.DOWN));
    }

    // for a given world, position, & direction, this returns a ConnectorType, which
    // indicates whether the cable should connect that block or not
    protected Boolean shouldConnect(BlockGetter world, BlockPos pos, Direction facing) {
        BlockPos otherPos = pos.relative(facing);
        BlockState otherState = world.getBlockState(otherPos);
        Block otherBlock = otherState.getBlock();

        // only connect to other cables (for now)
        return otherBlock instanceof ChubesCableBlock;
    }
}
