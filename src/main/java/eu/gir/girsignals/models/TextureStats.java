package eu.gir.girsignals.models;

import java.util.HashMap;
import java.util.Map;

import eu.gir.girsignals.GirsignalsMain;

public class TextureStats {

    private String blockstate;
    private Map<String, String> retexture;
    private Map<String, Map<String, String>> extentions;

    public boolean isautoBlockstate() {
        return blockstate == null;
    }

    public void resetStates(final String newblockstate, final Map<String, String> retexture) {
        
        this.blockstate = newblockstate;

        this.retexture = retexture;
    }

    public String getBlockstate() {
        return blockstate;
    }

    public Map<String, String> getRetextures() {
        return retexture;
    }

    /**
     * 1. String: Name of the file to get load 2. String: SEProperty 3. String: The
     * key of the retexture map
     */
    public Map<String, Map<String, String>> getExtentions() {
        return extentions;
    }

    public void appendExtention(final String seprop, final String enums, final String retexturekey,
            final String retexureval, final String modelname) {

        if (retexture == null) {

            retexture = new HashMap<>();

        }

        retexture.put(retexturekey, retexureval);

        if (blockstate.contains("with(prop.prop)")) {

            blockstate = blockstate.replace("prop.prop", seprop + "." + enums);

        } else {
            GirsignalsMain.log.warn("Unable to load an extention into " + modelname
                    + "! Did you included 'with(prop.prop)' to your blockstate?");
        }
    }
}
