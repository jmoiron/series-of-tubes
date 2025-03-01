package net.jmoiron.chubes.common.blocks;

import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;

public class ChubesCableBlock extends Block implements SimpleWaterloggedBlock {

    public static VoxelShape SHAPE_CABLE_NODE = Block.box(5, 5, 5, 11, 11, 11);

    public ChubesCableBlock(Block.Properties properties) {
        super(Properties.of()
                .noOcclusion()
                .forceSolidOn());
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

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return super.getRenderShape(state);
    }
}
