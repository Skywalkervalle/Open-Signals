package eu.gir.girsignals.signalbox.config;

import java.util.HashMap;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.enums.PathType;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

public interface ISignalAutoconfig {

    public static class ConfigInfo {
        public final SignalTileEnity current;
        public final SignalTileEnity next;
        public final int speed;
        public PathType type = PathType.NONE;

        public ConfigInfo(final SignalTileEnity current, final SignalTileEnity next,
                final int speed) {
            this.current = current;
            this.next = next;
            this.speed = speed;
        }

    }

    void change(final ConfigInfo info);

    void reset(final SignalTileEnity current);

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    default void changeIfPresent(final HashMap<SEProperty, Object> values,
            final SignalTileEnity current) {
        values.forEach((sep, value) -> current.getProperty(sep)
                .ifPresent(_u -> current.setProperty(sep, (Comparable) value)));
    }
}
