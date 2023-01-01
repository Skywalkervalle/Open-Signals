package com.troblecodings.signals.signalbox.debug;

import java.util.function.Consumer;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.signalbox.SignalBoxGrid;
import com.troblecodings.signals.signalbox.SignalBoxUtil.ConnectionChecker;
import com.troblecodings.signals.signalbox.entrys.PathOptionEntry;

public class DebugFactory extends SignalBoxFactory {

    private final boolean enableConnectionChecker = false;
    private final boolean enableDebugGrid = false;
    private final boolean enableDebugPathEntry = false;

    @Override
    public ConnectionChecker getConnectionChecker() {
        if (enableConnectionChecker)
            return new DebugChecker();
        return super.getConnectionChecker();
    }

    @Override
    public SignalBoxGrid getGrid(final Consumer<NBTWrapper> sendToAll) {
        if (enableDebugGrid)
            return new DebugGrid(sendToAll);
        return super.getGrid(sendToAll);
    }

    @Override
    public PathOptionEntry getEntry() {
        if (enableDebugPathEntry)
            return new DebugOptionEntry();
        return super.getEntry();
    }
}
