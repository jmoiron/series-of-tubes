package net.jmoiron.chubes.common.data;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.jmoiron.chubes.common.blocks.CableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ModelFile;

public class ChubesModels {

    public static void cableModel(DataGenContext<Block, CableBlock> ctx,
            RegistrateBlockstateProvider prov) {

        ModelFile m = prov.models()
            .sign(ctx.getName(), prov.modLoc("chubes/blocks/cable"));
        prov.simpleBlock(ctx.get(), m);
    }

}
