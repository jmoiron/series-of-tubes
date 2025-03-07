package net.jmoiron.chubes.common.data;

import net.minecraft.util.StringRepresentable;

public enum Channel implements StringRepresentable {
    BLACK,
    DARK_BLUE,
    DARK_GREEN,
    DARK_RED,
    DARK_PURPLE,
    GOLD,
    GRAY,
    DARK_GRAY,
    BLUE,
    GREEN,
    AQUA,
    RED,
    LIGHT_PURPLE,
    YELLOW,
    WHITE,
    NONE;

    public static final Channel[] CHANNELS = values();

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }

}
