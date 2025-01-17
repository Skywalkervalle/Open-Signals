package com.troblecodings.signals.signalbox.entrys;

import java.util.function.IntConsumer;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.core.ReadBuffer;
import com.troblecodings.signals.core.WriteBuffer;

public class IntegerEntry extends IPathEntry<Integer> implements IntConsumer {

    private int value = -1;

    @Override
    public void write(final NBTWrapper tag) {
        tag.putInteger(getName(), value);
    }

    @Override
    public void read(final NBTWrapper tag) {
        this.value = tag.getInteger(getName());
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(final Integer value) {
        this.value = value;
    }

    @Override
    public void accept(final int value) {
        this.setValue(value);
    }

    @Override
    public void readNetwork(final ReadBuffer buffer) {
        this.value = buffer.getInt();
    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putInt(value);
    }
}