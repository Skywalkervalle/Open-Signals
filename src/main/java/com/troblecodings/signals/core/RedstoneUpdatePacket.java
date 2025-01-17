package com.troblecodings.signals.core;

import java.util.Objects;

import com.troblecodings.signals.blocks.RedstoneInput;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class RedstoneUpdatePacket {

    public final Level world;
    public final BlockPos pos;
    public final boolean state;
    public final RedstoneInput block;

    public RedstoneUpdatePacket(final Level world, final BlockPos pos, final boolean state,
            final RedstoneInput block) {
        this.world = world;
        this.pos = pos;
        this.state = state;
        this.block = block;
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, pos, state, world);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RedstoneUpdatePacket other = (RedstoneUpdatePacket) obj;
        return Objects.equals(block, other.block) && Objects.equals(pos, other.pos)
                && state == other.state && Objects.equals(world, other.world);
    }
}