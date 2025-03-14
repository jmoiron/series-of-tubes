package net.jmoiron.chubes.common.data;

import static net.jmoiron.chubes.ChubesMod.REGISTRATE;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.jmoiron.chubes.common.blocks.CableBlock;
import net.jmoiron.chubes.common.blocks.entities.CableEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ChubesBlocks {

    public static final BlockEntry<CableBlock> CABLE_BLOCK = REGISTRATE
        .block("cable", CableBlock::new)
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .lang("cable")
        .blockstate((ctx, prov) -> ChubesModels.cableModel(ctx, prov))
        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
        .simpleItem()
        .register();

    public static final BlockEntityEntry<CableEntity> CABLE_ENTITY = REGISTRATE
        .blockEntity("cable", CableEntity::new)
        .validBlock(CABLE_BLOCK)
        .register();

    public static void init() {}

}
