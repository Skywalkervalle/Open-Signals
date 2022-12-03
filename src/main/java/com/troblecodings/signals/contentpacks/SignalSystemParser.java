package com.troblecodings.signals.contentpacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import eu.gir.girsignals.SignalsMain;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.blocks.Signal.SignalPropertiesBuilder;
import eu.gir.girsignals.models.parser.FunctionParsingInfo;
import eu.gir.girsignals.utils.FileReader;
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
            SignalsMain.log.error("Can't read out signalsystems from " + directory + "!");
            return properties;
        }
        
            systems.forEach((name, property) -> {
                try {
                    properties.put(name, GSON.fromJson(property, SignalSystemParser.class));
                } catch (final Exception e) {
                    SignalsMain.getLogger().error("File: " + name);
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
                .setRegistryName(SignalsMain.MODID, name).setUnlocalizedName(name);
    }
}