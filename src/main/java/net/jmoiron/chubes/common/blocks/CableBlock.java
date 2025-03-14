package net.jmoiron.chubes.common.blocks;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.lowdragmc.lowdraglib.gui.factory.BlockEntityUIFactory;
import com.mojang.authlib.properties.Property;

import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.jmoiron.chubes.common.blocks.entities.CableEntity;
import net.jmoiron.chubes.common.data.ChubesBlocks;
import net.jmoiron.chubes.common.data.ConnectorType;
import net.jmoiron.chubes.common.lib.Debug;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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

    // If a chubes cable is an endpoint, then it needs an entity..
    // HAS_ENTITY is a property that indicates that this cable has an entity.
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
    public static final VoxelShape SHAPE_CONN_UP = union(SHAPE_CABLE_UP, Block.box(4, 15, 4, 12, 16, 12));

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

        registerDefaultState(
            stateDefinition.any()
                .setValue(NORTH, ConnectorType.NONE)
                .setValue(SOUTH, ConnectorType.NONE)
                .setValue(WEST, ConnectorType.NONE)
                .setValue(EAST, ConnectorType.NONE)
                .setValue(DOWN, ConnectorType.NONE)
                .setValue(UP, ConnectorType.NONE)
                .setValue(HAS_ENTITY, false)
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
        return getState(pContext.getLevel(), pContext.getClickedPos(), null);
    }

    @Override
    // getShape returns a VoxelShape which is what determines the outline
    // you see in game that allows you to interact with the block. By making
    // it roughly match the size and shape of the cable, the player can do
    // things like stand on it at an appropriate height and interact with blocks
    // that are behind the visible shape of the cable.
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

    public static EnumProperty<ConnectorType> getProperty(Direction dir) {
        return switch (dir) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
            default -> NORTH;
        };
    }

    /*
    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter reader, BlockPos pos) {
        return SHAPE_CABLE_NODE;
    }
    */

    // needsEntity returns true if this cable block needs an entity. If
    // it is connected to an inventory, it needs an entity.
    private Boolean needsEntity(BlockState state) {
        return state.getProperties().stream()
            .anyMatch(prop -> state.getValue(prop).equals(ConnectorType.BLOCK));
    }

    @Nonnull
    @Override
    public BlockState updateShape(BlockState state, @Nonnull Direction direction, @Nonnull BlockState otherState,
                                  @Nonnull LevelAccessor world, @Nonnull BlockPos current, @Nonnull BlockPos offset) {
        // since our blockstate defines the shape of the pipe, updating the
        // blockstate updates the shape as well.
        return getState(world, current, state);
    }

    public BlockState getState(BlockGetter world, BlockPos pos, BlockState old) {
        var state = defaultBlockState()
            .setValue(NORTH, getConnector(world, pos, Direction.NORTH))
            .setValue(SOUTH, getConnector(world, pos, Direction.SOUTH))
            .setValue(WEST, getConnector(world, pos, Direction.WEST))
            .setValue(EAST, getConnector(world, pos, Direction.EAST))
            .setValue(UP, getConnector(world, pos, Direction.UP))
            .setValue(DOWN, getConnector(world, pos, Direction.DOWN))
            .setValue(WATERLOGGED, isWaterlogged(world, pos));

        // if this block doesn't need an entity then we're good
        if (old != null && old.getValue(HAS_ENTITY)) {
            state = state.setValue(HAS_ENTITY, true);
        }

        return state;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        // Check if a BlockEntity should be created
        if (needsEntity(state) && !state.getValue(HAS_ENTITY)) {
            BlockState newState = state.setValue(HAS_ENTITY, true);
            level.setBlock(pos, newState, Block.UPDATE_ALL);
            // trigger newBlockEntity
            level.getBlockEntity(pos);
        }
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (needsEntity(state) && state.getValue(HAS_ENTITY)) {
            return createBlockEntity(pos, state);
        }
        return null;
    }

    protected BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        System.out.println("Creating block entity for pos="+pos);
        System.out.println("state=" + state);
        Debug.printStack(10);
        return ChubesBlocks.CABLE_ENTITY.create(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CableEntity) {
                ((CableEntity) blockEntity).openCustomInventoryScreen(player);
            }
        }
        return InteractionResult.SUCCESS;
    }


    private Boolean isWaterlogged(BlockGetter world, BlockPos pos) {
        var fluidState = world.getFluidState(pos);
        return fluidState.is(FluidTags.WATER) && fluidState.getAmount() == 8;
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
}
