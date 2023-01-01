package com.troblecodings.signals.statehandler;

import java.util.Objects;

public class SignalStatePos {

    public final int file;
    public final long offset;

    public SignalStatePos(final int file, final long offset) {
        this.file = file;
        this.offset = offset;
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, offset);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SignalStatePos other = (SignalStatePos) obj;
        return file == other.file && offset == other.offset;
    }
}