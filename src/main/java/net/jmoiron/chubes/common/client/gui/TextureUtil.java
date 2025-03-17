package net.jmoiron.chubes.common.client.gui;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class TextureUtil {

    public static IGuiTexture getTextureForItem(String key) {
        var res = new ResourceLocation(key);
        var item = ForgeRegistries.ITEMS.getValue(res);
        if (item == null) {
            return null;
        }
        return new ItemStackTexture(item);
    }

}
