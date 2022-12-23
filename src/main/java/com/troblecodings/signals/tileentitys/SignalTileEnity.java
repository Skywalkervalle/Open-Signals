package com.troblecodings.signals.tileentitys;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.ILevelNameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.property.ExtendedBlockState;

public class SignalTileEnity extends SyncableTileEntity implements ILevelNameable, ISyncable {

    private final HashMap<SEProperty, String> map = new HashMap<>();

    public static final String PROPERTIES = "properties";
    public static final String CUSTOMNAME = "customname";
    public static final String BLOCKID = "blockid";

    private String formatCustomName = null;

    @Override
    public CompoundTag writeToNBT(final CompoundTag compound) {
        final CompoundTag comp = new CompoundTag();
        map.forEach((prop, in) -> prop.writeToNBT(comp, in));
        if (formatCustomName != null)
            comp.setString(CUSTOMNAME, formatCustomName);
        compound.put(PROPERTIES, comp);
        super.writeToNBT(compound);
        return compound;
    }

    private CompoundTag temporary = null;

    @Override
    public void readFromNBT(final CompoundTag compound) {
        super.readFromNBT(compound);
        final CompoundTag comp = compound.getCompoundTag(PROPERTIES);
        if (level == null) {
            temporary = comp.copy();
        } else {
            temporary = null;
            read(comp);
        }
    }

    private void read(final CompoundTag comp) {
        ((ExtendedBlockState) world.getBlockState(worldPosition).getBlock().getBlockState())
                .getUnlistedProperties().stream().forEach(prop -> {
                    final SEProperty sep = SEProperty.cst(prop);
                    sep.readFromNBT(comp).ifPresent(obj -> map.put(sep, obj));
                });
        if (comp.hasKey(CUSTOMNAME))
            setCustomName(comp.getString(CUSTOMNAME));
    }

    @Override
    public void onLoad() {
        if (temporary != null) {
            read(temporary);
            if (!level.isClientSide) {
                final BlockState state = level.getBlockState(worldPosition);
                level.notifyBlockUpdate(worldPosition, state, state, 3);
            }
            temporary = null;
        }
    }

    public void setProperty(final SEProperty prop, final String opt) {
        map.put(prop, opt);
        this.markDirty();
        getSignal().getUpdate(level, worldPosition);
    }

    public Map<SEProperty, Object> getProperties() {
        return ImmutableMap.copyOf(map);
    }

    public Optional<String> getProperty(final SEProperty prop) {
        if (map.containsKey(prop))
            return Optional.of(map.get(prop));
        return Optional.empty();
    }

    @Override
    public String getName() {
        if (formatCustomName == null)
            return getSignal().getSignalTypeName();
        return formatCustomName;
    }

    @Override
    public boolean hasCustomName() {
        return formatCustomName != null && getSignal().canHaveCustomname(this.map);
    }

    public void setCustomName(final String str) {
        this.formatCustomName = str;
        if (str == null && map.containsKey(Signal.CUSTOMNAME)) {
            map.remove(Signal.CUSTOMNAME);
        } else if (str != null) {
            map.put(Signal.CUSTOMNAME, true);
        }
        this.markDirty();
        this.syncClient();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderOverlay(final double x, final double y, final double z, final Font font) {
        getSignal().renderOverlay(x, y, z, this, font);
    }

    public Signal getSignal() {
        return (Signal) super.getBlockState().getBlock();
    }

    public int getBlockID() {
        return getSignal().getID();
    }

    @Override
    public void updateTag(final CompoundTag compound) {
        if (compound.contains(CUSTOMNAME)) {
            setCustomName(compound.getString(CUSTOMNAME));
            this.syncClient();
        }
    }

    @Override
    public CompoundTag getTag() {
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(formatCustomName, map, worldPosition, level);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SignalTileEnity other = (SignalTileEnity) obj;
        return Objects.equals(formatCustomName, other.formatCustomName)
                && Objects.equals(map, other.map)
                && Objects.equals(worldPosition, other.worldPosition)
                && Objects.equals(level, other.level);
    }

    @Override
    public boolean isValid(final Player player) {
        return true;
    }
}
