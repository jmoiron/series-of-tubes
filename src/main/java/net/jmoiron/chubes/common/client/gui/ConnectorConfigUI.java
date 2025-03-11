package net.jmoiron.chubes.common.client.gui;

import java.io.DataInputStream;

import com.lowdragmc.lowdraglib.gui.editor.data.UIProject;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.jmoiron.chubes.common.blocks.entities.CableEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;

public class ConnectorConfigUI {

    protected ModularUI ui;
    protected CableEntity ent;

    public ConnectorConfigUI(ModularUI ui, CableEntity ent) {
        this.ui = ui;
        this.ent = ent;
    }

    public WidgetGroup root() {
        return this.ui.mainGroup;
    }

    public static WidgetGroup loadWidgetGroupFromLoc(ResourceLocation loc) {
        ResourceManager manager = null;

        if (FMLEnvironment.dist.isClient()) {
            manager = Minecraft.getInstance().getResourceManager();
        } else if (ServerLifecycleHooks.getCurrentServer() != null) {
            manager = ServerLifecycleHooks.getCurrentServer().getResourceManager();
        } else {
            System.out.println("Don't know how to load the UI resource");
            return null;
        }

        try {
            var stream = manager.getResourceOrThrow(loc).open();
            var file = new DataInputStream(stream);
            var creator = UIProject.loadUIFromTag(NbtIo.read(file));
            return creator.get();
        } catch (Exception e) {
            System.out.println("Could not load resource " + loc);
            return null;
        }
    }
}
