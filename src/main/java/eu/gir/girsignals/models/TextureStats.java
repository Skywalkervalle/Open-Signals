package eu.gir.girsignals.models;

import java.util.HashMap;
import java.util.Map;

public class TextureStats {

    private float x = 0;
    private float y = 0;
    private float z = 0;

    private boolean loadOFFstate = true;
    private String blockstate;
    private Map<String, String> retexture;
    private Map<String, Map<String, String>> extentions;

    public float getOffsetX() {
        return x;
    }

    public float getOffsetY() {
        return y;
    }

    public float getOffsetZ() {
        return z;
    }

    public boolean loadOFFstate() {
        return loadOFFstate;
    }

    public boolean isautoBlockstate() {
        return blockstate == null;
    }

    public String getBlockstate() {
        return blockstate;
    }

    public void resetStates(final String state, final Map<String, String> retexture) {

        blockstate = state;

        this.retexture = retexture;
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

    /**
     * @return true if this model with these parameters should/can be loaded
     */

    public boolean appendExtention(final String seprop, final String enums,
            final String retexturekey, final String retexureval) {

        if (!loadOFFstate && enums.equalsIgnoreCase("OFF")) {
            return false;
        }

        if (this.retexture == null) {

            this.retexture = new HashMap<>();
        }
        this.retexture.put(retexturekey, retexureval);

        if (this.blockstate.length() < 4) {

            this.blockstate = "with(" + seprop + "." + enums + ")";

        } else {

            this.blockstate += " && with(" + seprop + "." + enums + ")";
        }

        return true;

    }
}
