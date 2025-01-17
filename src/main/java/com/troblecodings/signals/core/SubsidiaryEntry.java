package com.troblecodings.signals.core;

import java.util.Objects;

public class SubsidiaryEntry {

    public final SubsidiaryState enumValue;
    public final boolean state;

    public SubsidiaryEntry(final SubsidiaryState enumValue, final boolean state) {
        this.state = state;
        this.enumValue = enumValue;
    }

    public void writeNetwork(final WriteBuffer buffer) {
        enumValue.writeNetwork(buffer);
        buffer.putByte((byte) (state ? 1 : 0));
    }

    public static SubsidiaryEntry of(final ReadBuffer buffer) {
        return new SubsidiaryEntry(SubsidiaryState.of(buffer),
                buffer.getByte() == 1 ? true : false);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enumValue, state);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SubsidiaryEntry other = (SubsidiaryEntry) obj;
        return Objects.equals(enumValue, other.enumValue) && state == other.state;
    }
}