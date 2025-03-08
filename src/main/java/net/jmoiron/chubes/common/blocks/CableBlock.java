package net.jmoiron.chubes.common.blocks;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.jmoiron.chubes.common.data.ConnectorType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;

public class CableBlock extends Block implements SimpleWaterloggedBlock, EntityBlock {

    @Nonnull
    private static EnumProperty<ConnectorType> ctype(String name) {
        return EnumProperty.<ConnectorType>create(name, ConnectorType.class);
    }

    public static final EnumProperty<ConnectorType> NORTH = ctype("north");
    public static final EnumProperty<ConnectorType> SOUTH = ctype("south");
    public static final EnumProperty<ConnectorType> WEST = ctype("west");
    public static final EnumProperty<ConnectorType> EAST = ctype("east");
    public static final EnumProperty<ConnectorType> DOWN = ctype("down");
    public static final EnumProperty<ConnectorType> UP = ctype("up");

    // If a chubes cable is connected to
    public static final BooleanProperty HAS_ENTITY = BooleanProperty.create("has_entity");

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // the "node" of the cable is a centered 6x6x6 cube; it is the
    // default shape of a cable when it's placed next to non-cables
    public static final VoxelShape SHAPE_CABLE_NODE = Block.box(5, 5, 5, 11, 11, 11);

    // shapes for cable-to-cable connections; this part is called `cable_ext`
    // and it rotates into the cable node to connect to each cardinal direction
    public static final VoxelShape SHAPE_CABLE_NORTH = Block.box(5, 5, 0, 11, 11, 5);
    public static final VoxelShape SHAPE_CABLE_SOUTH = Block.box(5, 5, 11, 11, 11, 16);
    public static final VoxelShape SHAPE_CABLE_WEST = Block.box(0, 5, 5, 5, 11, 11);
    public static final VoxelShape SHAPE_CABLE_EAST = Block.box(11, 5, 5, 16, 11, 11);
    public static final VoxelShape SHAPE_CABLE_DOWN = Block.box(5, 0, 5, 11, 5, 11);
    public static final VoxelShape SHAPE_CABLE_UP = Block.box(5, 11, 5, 11, 16, 11);

    // shapes for cable-to-inventory connections; this part is called `cable_conn`
    // and it rotates into the cable with a little flange to indicate it is connected
    // to an inventory, like virtually all other pipe/conduit systems.
    public static final VoxelShape SHAPE_CONN_NORTH = union(SHAPE_CABLE_NORTH, Block.box(4, 4, 0, 12, 12, 1));
    public static final VoxelShape SHAPE_CONN_SOUTH = union(SHAPE_CABLE_SOUTH, Block.box(4, 4, 15, 12, 12, 16));
    public static final VoxelShape SHAPE_CONN_WEST = union(SHAPE_CABLE_WEST, Block.box(0, 4, 4, 1, 12, 12));
    public static final VoxelShape SHAPE_CONN_EAST = union(SHAPE_CABLE_EAST, Block.box(15, 4, 4, 16, 12, 12));
    public static final VoxelShape SHAPE_CONN_DOWN = union(SHAPE_CABLE_DOWN, Block.box(4, 0, 4, 12, 1, 12));
    public static final VoxelShape SHAPE_CONN_UP = union(SHAPE_CABLE_UP, Block.box(4, 12, 4, 12, 16, 12));

    private static List<Triple<EnumProperty<ConnectorType>, VoxelShape, VoxelShape>> PROP_SHAPES = Arrays.asList(
        new ImmutableTriple<>(NORTH, SHAPE_CABLE_NORTH, SHAPE_CONN_NORTH),
        new ImmutableTriple<>(SOUTH, SHAPE_CABLE_SOUTH, SHAPE_CONN_SOUTH),
        new ImmutableTriple<>(WEST, SHAPE_CABLE_WEST, SHAPE_CONN_WEST),
        new ImmutableTriple<>(EAST, SHAPE_CABLE_EAST, SHAPE_CONN_EAST),
        new ImmutableTriple<>(DOWN, SHAPE_CABLE_DOWN, SHAPE_CONN_DOWN),
        new ImmutableTriple<>(UP, SHAPE_CABLE_UP, SHAPE_CONN_UP)
    );

    private static VoxelShape union(VoxelShape s1, VoxelShape s2) {
        return Shapes.join(s1, s2, BooleanOp.OR);
    }

    public CableBlock(Block.Properties properties) {
        super(Properties.of()
                .noOcclusion()
                .forceSolidOn()
                .strength(1.0f)
        );

        // at construction, a cable is not connected to anything and not waterlogged
        // XXX: is this too many blockstates?
        registerDefaultState(
            stateDefinition.any()
                .setValue(NORTH, ConnectorType.NONE)
                .setValue(SOUTH, ConnectorType.NONE)
                .setValue(WEST, ConnectorType.NONE)
                .setValue(EAST, ConnectorType.NONE)
                .setValue(DOWN, ConnectorType.NONE)
                .setValue(UP, ConnectorType.NONE)
                .setValue(WATERLOGGED, false)
        );
    }

    // the blockstate for this class has to be defined in order for
    // us to be able to set values for it, the class machinery does
    // not use some kind of introspection to auto-generate the blockstate
    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_ENTITY, NORTH, SOUTH, WEST, EAST, DOWN, UP, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        var current = super.getStateForPlacement(pContext);
        return getConnectorState(pContext.getLevel(), pContext.getClickedPos(), current);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // TODO: pre-cache voxelshapes so we don't have to re-calculate all the time.

        // the base shape is SHAPE_CABLE_NODE, and we add to that segments
        // for the cardinal directions based on their connector type
        VoxelShape shape = SHAPE_CABLE_NODE;

        for(var propShape : PROP_SHAPES) {
            var val = state.getValue(propShape.getLeft());
            if (val == ConnectorType.CABLE) {
                shape = union(shape, propShape.getMiddle());
            } else if (val == ConnectorType.BLOCK) {
                shape = union(shape, propShape.getRight());
            }
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

    public BlockState getConnectorState(BlockGetter world, BlockPos pos, BlockState state) {
        return state
            .setValue(NORTH, getConnector(world, pos, Direction.NORTH))
            .setValue(SOUTH, getConnector(world, pos, Direction.SOUTH))
            .setValue(WEST, getConnector(world, pos, Direction.WEST))
            .setValue(EAST, getConnector(world, pos, Direction.EAST))
            .setValue(UP, getConnector(world, pos, Direction.UP))
            .setValue(DOWN, getConnector(world, pos, Direction.DOWN));
    }

    // getConnector returns the connector type for the block in the direction of
    // facing from pos. The return can either be CABLE (if it's a chube), BLOCK if
    // it has any forge item/fluid capabilities, and NONE otherwise.
    protected ConnectorType getConnector(BlockGetter world, BlockPos pos, Direction facing) {
        var otherPos = pos.relative(facing);
        var otherState = world.getBlockState(otherPos);

        // air
        if (otherState == null) {
            return ConnectorType.NONE;
        } else if (otherState.getBlock() instanceof CableBlock) {
            return ConnectorType.CABLE;
        }

        // the other block is a block and not a chube or air
        var otherEntity = world.getBlockEntity(otherPos);

        if (otherEntity == null) {
            return ConnectorType.NONE;
        }

        if (otherEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).isPresent()) {
            return ConnectorType.BLOCK;
        }
        if (otherEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
            return ConnectorType.BLOCK;
        }

        return ConnectorType.NONE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        // TODO: create a new block entity when this block is connected
        // to another block that has an inventory.
        return null;
    }
}
