package com.troblecodings.signals.contentpacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.Signal.SignalPropertiesBuilder;
import com.troblecodings.signals.models.parser.FunctionParsingInfo;
import com.troblecodings.signals.utils.FileReader;

import net.minecraftforge.common.property.IUnlistedProperty;

public class SignalSystemParser {

    private SignalPropertiesBuilder systemProperties;
    private List<SEPropertyParser> seProperties;

    private static transient final Gson GSON = new Gson();

    public List<SEPropertyParser> getSEProperties() {
        return seProperties;
    }

    public static Map<String, SignalSystemParser> getSignalSystems(final String directory) {

        final Map<String, String> systems = FileReader.readallFilesfromDierectory(directory);

        final Map<String, SignalSystemParser> properties = new HashMap<>();

        if (systems == null) {
            OpenSignalsMain.log.error("Can't read out signalsystems from " + directory + "!");
            return properties;
        }
        
            systems.forEach((name, property) -> {
                try {
                    properties.put(name, GSON.fromJson(property, SignalSystemParser.class));
                } catch (final Exception e) {
                    OpenSignalsMain.getLogger().error("File: " + name);
                    e.printStackTrace();
                }
            });

        return properties;
    }

    @SuppressWarnings("rawtypes")
    public Signal createSignalSystem(final String fileName) {

        final String name = fileName.replace(".json", "").replace("_", "");

        final List<IUnlistedProperty> properties = new ArrayList<>();

        final FunctionParsingInfo info = new FunctionParsingInfo(name, properties);
        seProperties.forEach(prop -> properties.add(prop.createSEProperty(info)));
        Signal.nextConsumer = list -> {
            list.addAll(properties);
        };

        return (Signal) new Signal(systemProperties.typename(name).build(info))
                .setRegistryName(OpenSignalsMain.MODID, name).setUnlocalizedName(name);
    }
}