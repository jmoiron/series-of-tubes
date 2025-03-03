package net.jmoiron.chubes.common;

import net.minecraft.util.StringRepresentable;

public enum ConnectorType implements StringRepresentable {
    NONE,
    CABLE,
    BLOCK;

    public static final ConnectorType[] TYPES = values();

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }
}
