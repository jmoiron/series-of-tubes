package net.jmoiron.chubes.common.client.gui.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.jmoiron.chubes.common.client.gui.TextureUtil;
import net.minecraft.network.FriendlyByteBuf;

// NOTE: may be it doesn't matter if we have this stuff available in the GUI editor
// It might be nice to design it there, but it seems like it might eventually be
// easier to just create the widgets in Java as it is a bit fidgety to get all of
// the sizing and spacing right in the editor.

@LDLRegister(name="cyclingButton", group="widget.basic", modID="chubes")
public class CyclingButtonWidget extends Widget {

    protected IGuiTexture baseTexture;
    protected IGuiTexture hoverTexture;

    protected List<IGuiTexture> states = new ArrayList<IGuiTexture>();
    protected IntConsumer onClicked;

    protected int state;

    // XXX: do I need this? I have no idea what this is for.
    protected IntSupplier supplier;

    public CyclingButtonWidget() {
        this(0, 0, 18, 18);
    }

    public CyclingButtonWidget(int x, int y, int h, int w) {
        super(x, y, h, w);
        this.baseTexture = ResourceBorderTexture.BUTTON_COMMON;
        this.hoverTexture = new ColorBorderTexture(1, -1);
        state = 0;
    }

    public CyclingButtonWidget setCallback(IntConsumer callback) {
        onClicked = callback;
        return this;
    }

    public CyclingButtonWidget addState(IGuiTexture... textures) {
        Collections.addAll(states, textures);
        return this;
    }

    public void setState(int newState) {
        /*
        if (state == newState) {
            return;
        }
        */

        if (supplier != null) {
            System.out.println("SetState > Supplier wasn't null somehow");
        }

        state = newState;

        if (isRemote()) {
            writeClientAction(2, buffer -> buffer.writeInt(state));
        } else if (states.size() == 0) {
            setBackground(new GuiTextureGroup(baseTexture));
        } else {
            setBackground(new GuiTextureGroup(baseTexture, states.get(state)));
            writeUpdateInfo(2, buffer -> buffer.writeInt(state));
        }
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        super.writeInitialData(buffer);
        buffer.writeVarInt(state);
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        setState(buffer.readVarInt());
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        /*
        if (!isClientSideWidget && indexSupplier != null) {
            var newIndex = indexSupplier.getAsInt();
            if (newIndex != index) {
                index = newIndex;
                writeUpdateInfo(1, buf -> buf.writeVarInt(index));
            }
        }
        */
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        /*
        if (isClientSideWidget && indexSupplier != null) {
            var newIndex = indexSupplier.getAsInt();
            if (newIndex != index) {
                index = newIndex;
                setBackground(texture.get(index));
            }
        }
        */
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (isMouseOverElement(x, y)) {
            state = (state+1) % states.size();
            setState(state);
            if (onClicked != null ) {
                onClicked.accept(state);
            }
            playButtonClickSound();
            return true;
        }
        return false;
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            state = buffer.readVarInt();
            if (onClicked != null) {
                onClicked.accept(state);
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        switch (id) {
            case 1 -> setState(buffer.readVarInt());
            default -> super.readUpdateInfo(id, buffer);
        }
    }

}
