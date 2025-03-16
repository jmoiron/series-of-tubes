package net.jmoiron.chubes.common.client.gui.widget;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

// NOTE: may be it doesn't matter if we have this stuff available in the GUI editor
// It might be nice to design it there, but it seems like it might eventually be
// easier to just create the widgets in Java as it is a bit fidgety to get all of
// the sizing and spacing right in the editor.

@LDLRegister(name = "cycling_button", group = "widget.basic")
public class CyclingButtonWidget extends Widget implements IConfigurableWidget {

    protected IGuiTexture baseTexture;
    protected IGuiTexture hoverTexture;

    protected List<IGuiTexture> states;
    protected List<Supplier<ClickData>> onClicked;

    protected Integer state;

    // XXX: do I need this? I'm not sure, it seems mostly useful
    // for the gui editor.
    protected Supplier<Integer> supplier;

    public CyclingButtonWidget() {
        this(0, 0, 18, 18);
    }

    public CyclingButtonWidget(int x, int y, int h, int w) {
        super(x, y, h, w);
        this.baseTexture = ResourceBorderTexture.BUTTON_COMMON;
        this.hoverTexture = new ColorBorderTexture(1, -1);
        state = 0;
    }

    public CyclingButtonWidget addState(IGuiTexture texture, Supplier<ClickData> callback) {
        states.add(texture);
        onClicked.add(callback);
        return this;
    }

    public CyclingButtonWidget setState(Integer newState) {
        if (state == newState) {
            return this;
        }
        state = newState;
        if (gui == null) {
            return this;
        }
        if (isRemote()) {
            writeClientAction(2, buffer -> buffer.writeInt(state));
        } else {
            writeUpdateInfo(2, buffer -> buffer.writeInt(state));
        }

        return this;
    }
}
