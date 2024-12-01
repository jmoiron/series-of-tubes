package net.jmoiron.sot.common.data;

import com.tterrag.registrate.util.entry.BlockEntry;

import net.jmoiron.sot.common.blocks.BundleBlock;

import static net.jmoiron.sot.SeriesOfTubesMod.REGISTRATE;

public class SotBlocks {

    public static final BlockEntry<BundleBlock> BUNDLE = REGISTRATE
        .block("bundle", BundleBlock::new)
        .lang("Bundle")
        .simpleItem()
        .register();
}
