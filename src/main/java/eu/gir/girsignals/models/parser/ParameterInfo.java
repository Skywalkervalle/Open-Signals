package eu.gir.girsignals.models.parser;

import java.util.List;

import eu.gir.girsignals.blocks.Signal;
import net.minecraftforge.common.property.IUnlistedProperty;

public class ParameterInfo {
    public String argument;
    public Signal system;
    @SuppressWarnings("rawtypes")
    public final List<IUnlistedProperty> properties;

    public ParameterInfo(final String argument, final Signal system) {
        this.argument = argument;
        this.system = system;
        this.properties = system.getProperties();
    }

    public ParameterInfo(final String argument, final ParameterInfo info) {
        this.argument = argument;
        this.system = info.system;
        this.properties = info.properties;
    }
}