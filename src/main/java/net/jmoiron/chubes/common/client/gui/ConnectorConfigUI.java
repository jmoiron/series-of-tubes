package net.jmoiron.chubes.common.client.gui;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import com.lowdragmc.lowdraglib.gui.editor.data.UIProject;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture.TextType;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.TabButton;
import com.lowdragmc.lowdraglib.gui.widget.TabContainer;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.jmoiron.chubes.common.blocks.CableBlock;
import net.jmoiron.chubes.common.blocks.entities.CableEntity;
import net.jmoiron.chubes.common.data.ConnectorType;
import net.jmoiron.chubes.common.lib.Debug;
import net.jmoiron.chubes.common.client.gui.widget.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

public class ConnectorConfigUI {

    public static final ResourceTexture TABS_LEFT = new ResourceTexture("ldlib:textures/gui/tabs_left.png");

    public static final List<Direction> DIRECTIONS = Arrays.asList(
        Direction.NORTH, Direction.SOUTH, Direction.EAST,
        Direction.WEST, Direction.UP, Direction.DOWN
    );

    public static final List<ResourceTexture> TAB_TEXTURES = Arrays.asList(
        TABS_LEFT.getSubTexture(0, 0, 0.5f, 1f / 3),
        TABS_LEFT.getSubTexture(0.5f, 0, 0.5f, 1f / 3),
        TABS_LEFT.getSubTexture(0, 1f / 3, 0.5f, 1f / 3),
        TABS_LEFT.getSubTexture(0.5f, 1f / 3, 0.5f, 1f / 3)
    );

    protected ModularUI ui;
    protected CableEntity ent;

    public ConnectorConfigUI(ModularUI ui, CableEntity ent) {
        this.ui = ui;
        this.ent = ent;
        init();
    }

    private static void log(String str) {
        System.out.println("<client="+FMLEnvironment.dist.isClient()+"> "+str);
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

    public void init() {
        Widget tabContent = ui.getFirstWidgetById("tab_content");

        if (tabContent == null) {
            System.out.println("widget is null");
            return;
        }
        if (!(tabContent instanceof TabContainer)) {
            System.out.println("tab_content is not a WidgetGroup");
            return;
        }

        TabContainer tc = (TabContainer)tabContent;
        System.out.println("mainGroup");
        //printWidgetTree(ui.mainGroup, 0);

        initTabTextures(tc);
        initCyclingButton();
        if (FMLEnvironment.dist.isClient()) {
            initSlotTextures();
        }

    }

    private void initCyclingButton() {
        var cbw = new CyclingButtonWidget(0, -25, 20, 20)
            .addState(TextureUtil.getTextureForItem("gtceu:steel_ingot").scale(0.66f))
            .addState(TextureUtil.getTextureForItem("minecraft:water_bucket").scale(0.66f))
            .setCallback(i -> {
                log("State set to " + i);
            });

        replaceWidget(ui.getFirstWidgetById("cyclebutton"), cbw);
    }

    private void initSlotTextures() {
        var itemPipe = (SlotWidget)ui.getFirstWidgetById("item_pipe");
        var fluidPipe = (SlotWidget)ui.getFirstWidgetById("fluid_pipe");

        var pipeSilk = TextureUtil
            .getSilkScreenTextureForItem("gtceu:stainless_steel_normal_fluid_pipe")
            .scale(0.90f);

        // steel_normal_fluid_pipe")

        var itemSilk = TextureUtil.getSilkScreenTextureForItem("gtceu:steel_ingot").scale(0.9f);
        var fluidSilk = TextureUtil.getSilkScreenTextureForItem("minecraft:water_bucket").scale(0.9f);

        itemPipe.setBackground(
            new GuiTextureGroup(itemPipe.ITEM_SLOT_TEXTURE, pipeSilk)
        );

        fluidPipe.setBackground(
            new GuiTextureGroup(fluidPipe.ITEM_SLOT_TEXTURE, fluidSilk)
        );
    }

    private void initTabTextures(TabContainer tc) {
        for (int i=0; i<DIRECTIONS.size(); i++) {
            Direction dir = DIRECTIONS.get(i);
            System.out.println("pos="+i+" dir="+dir);
            TabButton tab = (TabButton)tc.buttonGroup.widgets.get(i);

            var abbr = dir.getName().substring(0, 1).toUpperCase();
            var adjItemStack = getAdjacentStack(dir);
            IGuiTexture tex;

            if (adjItemStack.isEmpty()) {
                tex = new TextTexture(abbr);
            } else {
                tex = new GuiTextureGroup(
                    new ItemStackTexture(adjItemStack).scale(0.50f),
                    new TextTexture(abbr).setDropShadow(true)
                );
            }

            // top tab gets the first two TAB_TEXTURES, and subsequent ones
            // get the subsequent TAB_TEXTURES.
            int idx = i == 0 ? 0 : 2;
            tab.setTexture(
                new GuiTextureGroup(TAB_TEXTURES.get(idx), tex),
                new GuiTextureGroup(TAB_TEXTURES.get(idx+1), tex)
            );
        }
    }

    private void replaceWidget(Widget out, Widget in) {
        log(String.format("out.pos=%s out.selfPos=%s in.pos=%s in.selfPos=%s",
            out.getPosition(),
            out.getSelfPosition(),
            in.getPosition(),
            in.getSelfPosition()));

        ui.mainGroup.removeWidget(out);
        ui.mainGroup.addWidget(in);
    }


    private ItemStack getAdjacentStack(Direction dir) {
        // if the cable does not have ConnectorType.BLOCK, then there's
        // no reason to draw an icon.
        var conType = ent.getBlockState().getValue(CableBlock.getProperty(dir));
        if (conType != ConnectorType.BLOCK) {
            return ItemStack.EMPTY;
        }
        var adjPos = ent.getBlockPos().relative(dir);
        var adjState = ent.getLevel().getBlockState(adjPos);
        return adjState.getBlock().getCloneItemStack(ent.getLevel(), adjPos, adjState);
    }

    public void printWidgetTree(Widget w, int depth) {
        var indent = "  ".repeat(depth);
        if (w instanceof TabContainer) {
            System.out.printf("%s > TabContainer id=%s\n", indent, w.getId());
            System.out.printf("%s > Buttons:\n", indent);
            ((TabContainer)w).buttonGroup.widgets.stream().forEach(x -> printWidgetTree(x, depth+1));
            System.out.printf("%s > Containers:\n", indent);
            ((TabContainer)w).containerGroup.widgets.stream().forEach(x -> printWidgetTree(x, depth+1));
        } if (w instanceof WidgetGroup) {
            System.out.printf("%s > WidgetGroup id=%s\n", indent, w.getId());
            ((WidgetGroup)w).widgets.stream().forEach(x -> printWidgetTree(x, depth+1));
        } else {
            System.out.printf("%s > %s\n", indent, Debug.fmtWidgetData(w));
        }
    }

}
