package net.jmoiron.chubes.common.data;

import static net.jmoiron.chubes.ChubesMod.REGISTRATE;

import com.tterrag.registrate.util.entry.BlockEntry;

import net.jmoiron.chubes.common.blocks.ChubesCableBlock;
import net.minecraft.tags.BlockTags;

public class ChubesBlocks {

    public static final BlockEntry<ChubesCableBlock> ChubesCable = REGISTRATE
        .block("cable", ChubesCableBlock::new)
        .lang("cable")
        .blockstate((ctx, prov) -> ChubesModels.cableModel(ctx, prov))
        .tag(BlockTags.MINEABLE_WITH_PICKAXE)
        .simpleItem()
        .register();

    public static void init() {}

}
