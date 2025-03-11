package net.jmoiron.chubes.common.blocks.entities;

import java.io.DataInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.editor.data.UIProject;
import com.lowdragmc.lowdraglib.gui.factory.BlockEntityUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.IManaged;
import com.lowdragmc.lowdraglib.syncdata.IManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAsyncAutoSyncBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAutoPersistBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.jmoiron.chubes.ChubesMod;
import net.jmoiron.chubes.common.client.gui.ConnectorConfigUI;
import net.jmoiron.chubes.common.data.Channel;
import net.jmoiron.chubes.common.data.ChubesBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class CableEntity extends BlockEntity implements HasCustomInventoryScreen, IUIHolder, IAsyncAutoSyncBlockEntity, IAutoPersistBlockEntity, IManaged {
    // When a cable connects to a bock, it needs an entity because it will have
    // inventories and other data for its configuration

    protected class ConnectorConfig {
        // Sided configuration items and channel config is
        // maintained in a flat list using offsets to simplify
        // server/client syncing. This class provides semantic
        // access to make the inv & cfg usage more readable.
        public ItemStackHandler Inventory;
        public List<Channel> Channels;
        public List<Boolean> forceDisconnected;

        public ConnectorConfig() {
            var sides = Direction.values().length;
            // {pipe,filter} for {item,fluid} per side
            Inventory = new ItemStackHandler(sides*4);
            // {item,fluid} channel per side
            Channels = NonNullList.withSize(sides*2, Channel.NONE);
            // one per side
            forceDisconnected = NonNullList.withSize(sides, false);
        }

        // getContainer returns a container with all of the items across all of
        // this config's inventory slots.
        public SimpleContainer getContainer() {
            SimpleContainer container = new SimpleContainer(Inventory.getSlots());
            for (int i = 0; i<Inventory.getSlots(); i++){
                container.addItem(Inventory.getStackInSlot(i));
            }
            return container;
        }

        public void serializeNBT(CompoundTag pTag) {
            pTag.put("inventory", Inventory.serializeNBT());
            // serialize channels & forceDisconnected as an array of integers
            List<Integer> channelsInts = Channels.stream().map(channel -> channel.ordinal()).toList();
            List<Integer> forceDisconnectedInts = forceDisconnected.stream().map(b -> b ? 1 : 0).toList();
            pTag.put("channels", new IntArrayTag(channelsInts));
            pTag.put("forceDisconnected", new IntArrayTag(forceDisconnectedInts));
        }

        // load configuration + inventory contents from nbt
        public void load(CompoundTag pTag) {
            Inventory.deserializeNBT(pTag);

            Channels = Arrays.stream(pTag.getIntArray("channels"))
                .mapToObj(ic -> Channel.CHANNELS[ic])
                .collect(Collectors.toList());

            forceDisconnected = Arrays.stream(pTag.getIntArray("forceDisconnected"))
                .mapToObj(val -> val != 0)
                .collect(Collectors.toList());
        }
    }

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CableEntity.class);
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public ConnectorConfig config;
    public ConnectorConfigUI ui;

    public CableEntity(BlockEntityType<?> in, BlockPos pos, BlockState state) {
        super(in, pos, state);
        config = new ConnectorConfig();
    }


    // next three methods are needed for item handler, but I'm not sure
    // we need to expose the capability here, as I think it makes our
    // config slots look like an accessible inventory
    @Override
    public @Nonnull <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> config.Inventory);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }


    // when this cable is broken, it should drop all of the config ItemStacks
    // in its configuration inventories.
    public void drops() {
        Containers.dropContents(this.level, this.worldPosition, config.getContainer());
    }

    @Override
    public void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        config.serializeNBT(pTag);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        config.load(pTag);
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            BlockEntityUIFactory.INSTANCE.openUI(this, serverPlayer);
        }
    }

    private WidgetGroup createWidgetGroup() {
        ResourceLocation uiFile = new ResourceLocation(ChubesMod.MOD_ID, "ui/connectorcfg.ui");
        return ConnectorConfigUI.loadWidgetGroupFromLoc(uiFile);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        var wg = this.createWidgetGroup();
        var modularUI = new ModularUI(wg, this, entityPlayer);
        this.ui = new ConnectorConfigUI(modularUI, this);
        return modularUI;
    }


    @Override
    public boolean isInvalid() {
        return this.isRemoved();
    }


    @Override
    public boolean isRemote() {
        var level = this.getLevel();
        return level == null ? LDLib.isRemote() : !level.isClientSide();
    }


    @Override
    public void markAsDirty() {
        this.setChanged();
    }


    // Method overrides for LDLib auto sync storage
    // https://low-drag-mc.github.io/LowDragMC-Doc/ldlib/SyncData/

    @Override
    public IManagedStorage getRootStorage() {
        return getSyncStorage();
    }

    @Override
    public IManagedStorage getSyncStorage() {
        return syncStorage;
    }


    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {
        setChanged();
    }

}
