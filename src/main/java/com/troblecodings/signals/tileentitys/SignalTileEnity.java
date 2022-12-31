package com.troblecodings.signals.tileentitys;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;

import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SignalTileEnity extends SyncableTileEntity implements NamableWrapper, ISyncable {

    public static final String PROPERTIES = "properties";
    public static final String CUSTOMNAME = "customname";
    public static final String BLOCKID = "blockid";

    private final HashMap<SEProperty, String> map = new HashMap<>();
    private String formatCustomName = null;
    private NBTWrapper temporary = null;

    public SignalTileEnity(final BlockEntityType<?> blockType, final BlockPos blockPos,
            final BlockState state) {
        super(blockType, blockPos, state);
    }

    @Override
    public void saveWrapper(NBTWrapper wrapper) {
    	final NBTWrapper properties = new NBTWrapper();
        map.forEach((prop, in) -> prop.writeToNBT(properties, in));
        if (formatCustomName != null)
        	properties.putString(CUSTOMNAME, formatCustomName);
        wrapper.putWrapper(PROPERTIES, properties);
    }
    
    @Override
    public void loadWrapper(NBTWrapper wrapper) {
        final NBTWrapper properties = wrapper.getWrapper(PROPERTIES);
        if (level == null) {
            temporary = properties.copy();
        } else {
            temporary = null;
            read(properties);
        }
    }
    
    private void read(final NBTWrapper comp) {
    	// TODO read
        if (comp.contains(CUSTOMNAME))
            setCustomName(comp.getString(CUSTOMNAME));
    }

    @Override
    public void onLoad() {
        if (temporary != null) {
            read(temporary);
            if (!level.isClientSide) {
                final BlockState state = level.getBlockState(worldPosition);
                // TODO update
            }
            temporary = null;
        }
    }

    public void setProperty(final SEProperty prop, final String opt) {
        map.put(prop, opt);
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
    public String getNameWrapper() {
        if (formatCustomName == null)
            return getSignal().getSignalTypeName();
        return formatCustomName;
    }

    @Override
    public boolean hasCustomName() {
        return formatCustomName != null && getSignal().canHaveCustomname();
    }

    public void setCustomName(final String str) {
        this.formatCustomName = str;
        if (str == null && map.containsKey(Signal.CUSTOMNAME)) {
            map.remove(Signal.CUSTOMNAME);
        } else if (str != null) {
            map.put(Signal.CUSTOMNAME, "TRUE");
        }
        this.syncClient();
    }

    @OnlyIn(Dist.CLIENT)
    public void renderOverlay(final double x, final double y, final double z, final Font font) {
        getSignal().renderOverlay(x, y, z, this, font);
    }

    public Signal getSignal() {
        return (Signal) super.getBlockState().getBlock();
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
