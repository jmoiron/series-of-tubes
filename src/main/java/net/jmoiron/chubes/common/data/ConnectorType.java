package net.jmoiron.chubes.common.data;

import net.minecraft.util.StringRepresentable;

public enum ConnectorType implements StringRepresentable {
    CABLE,
    BLOCK,
    NONE;

    public static final ConnectorType[] TYPES = values();

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }

    public Boolean isConnected() {
        return this != NONE;
    }
}
