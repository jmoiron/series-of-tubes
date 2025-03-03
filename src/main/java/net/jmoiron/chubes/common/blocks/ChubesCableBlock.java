package net.jmoiron.chubes.common.blocks;

import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
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
        // TODO: other cable shapes
        return SHAPE_CABLE_NODE;
    }

    /*
    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return SHAPE_CABLE_NODE;
    }
    */

    // allow the block to drop itself when mined
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return super.getDrops(state, builder);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return super.getRenderShape(state);
    }
}
