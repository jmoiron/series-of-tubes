package net.jmoiron.chubes.common;

import net.minecraft.util.StringRepresentable;

public enum ConnectionType implements StringRepresentable {
    NONE,
    BUNDLE,
    BLOCK;

    public static final ConnectionType[] TYPES = values();

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
