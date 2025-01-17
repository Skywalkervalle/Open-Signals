package com.troblecodings.signals.tileentitys;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.RedstoneInput;
import com.troblecodings.signals.core.LinkingUpdates;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.RedstoneUpdatePacket;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.handler.SignalBoxHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneIOTileEntity extends SyncableTileEntity implements ISyncable {

    public RedstoneIOTileEntity(final TileEntityInfo info) {
        super(info);
    }

    public static final String NAME_NBT = "name";
    public static final String LINKED_LIST = "linkedList";

    @Override
    public String getNameWrapper() {
        final String name = super.getNameWrapper();
        return name == null || name.isEmpty()
                ? this.getBlockState().getBlock().getRegistryName().getPath()
                : name;
    }

    @Override
    public void saveWrapper(final NBTWrapper wrapper) {
        wrapper.putList(LINKED_LIST,
                linkedPositions.stream().map(NBTWrapper::getBlockPosWrapper).toList());
    }

    @Override
    public void loadWrapper(final NBTWrapper wrapper) {
        linkedPositions.clear();
        wrapper.getList(LINKED_LIST).stream().map(NBTWrapper::getAsPos)
                .forEach(linkedPositions::add);
    }

    public void sendToAll() {
        if (level.isClientSide)
            return;
        final boolean power = this.level.getBlockState(this.worldPosition)
                .getValue(RedstoneIO.POWER);
        final RedstoneUpdatePacket update = new RedstoneUpdatePacket(level, worldPosition, power,
                (RedstoneInput) this.getBlockState().getBlock());
        linkedPositions.forEach(
                pos -> SignalBoxHandler.updateInput(new PosIdentifier(pos, level), update));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level == null || level.isClientSide)
            return;
        final LinkingUpdates update = SignalBoxHandler
                .getPosUpdates(new PosIdentifier(worldPosition, level));
        if (update == null)
            return;
        update.getPosToRemove().forEach(pos -> unlink(pos));
        update.getPosToAdd().forEach(pos -> link(pos));
        if (SignalBoxHandler.containsOutputUpdates(new PosIdentifier(worldPosition, level))) {
            BlockState state = level.getBlockState(worldPosition);
            state = state.setValue(RedstoneIO.POWER,
                    SignalBoxHandler.getNewOutputState(new PosIdentifier(worldPosition, level)));
            level.setBlockAndUpdate(worldPosition, state);
        }
    }

    public void link(final BlockPos pos) {
        if (level.isClientSide)
            return;
        if (!linkedPositions.contains(pos))
            linkedPositions.add(pos);
        this.syncClient();
    }

    public void unlink(final BlockPos pos) {
        if (level.isClientSide)
            return;
        if (linkedPositions.contains(pos))
            linkedPositions.remove(pos);
        this.syncClient();
    }

    @Override
    public boolean isValid(final Player player) {
        return true;
    }
}
