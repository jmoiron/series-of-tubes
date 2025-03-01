package net.jmoiron.chubes.common.data;

import static net.jmoiron.chubes.ChubesMod.REGISTRATE;

import com.tterrag.registrate.util.entry.BlockEntry;

import net.jmoiron.chubes.common.blocks.ChubesCableBlock;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class ChubesBlocks {

    public static final BlockEntry<ChubesCableBlock> ChubesCable = REGISTRATE
        .block("cable", ChubesCableBlock::new)
        .initialProperties(() -> Blocks.IRON_BLOCK)
        .lang("cable")
        .blockstate((ctx, prov) -> ChubesModels.cableModel(ctx, prov))
        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
        .simpleItem()
        .register();

    public static void init() {}

}
