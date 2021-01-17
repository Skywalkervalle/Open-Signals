package net.gir.girsignals.blocks;

import net.gir.girsignals.EnumSignals.HPVR;
import net.gir.girsignals.EnumSignals.MAST_SIGN;
import net.gir.girsignals.EnumSignals.ZS32;
import net.gir.girsignals.SEProperty;
import net.gir.girsignals.SEProperty.ChangeableStage;

public class SignalHV extends SignalBlock {

	public SignalHV() {
		super("HV");
	}

	public static final SEProperty<HPVR> STOPSIGNAL = SEProperty.of("stopsignal", HPVR.OFF);
	public static final SEProperty<HPVR> DISTANTSIGNAL = SEProperty.of("distantsignal", HPVR.OFF);
	public static final SEProperty<Boolean> VR_LIGHT = SEProperty.of("vrlight", false);
	public static final SEProperty<Boolean> NE2 = SEProperty.of("ne2", false);
	public static final SEProperty<MAST_SIGN> MASTSIGN = SEProperty.of("mastsign", MAST_SIGN.OFF, ChangeableStage.GUISTAGE);
	public static final SEProperty<ZS32> ZS3 = SEProperty.of("zs3", ZS32.OFF);
	public static final SEProperty<ZS32> ZS3V = SEProperty.of("zs3v", ZS32.OFF);
	public static final SEProperty<Boolean> ZS1 = SEProperty.of("zs1", false);
	public static final SEProperty<Boolean> ZS7 = SEProperty.of("zs7", false);
	public static final SEProperty<Boolean> CUSTOMNAME = SEProperty.of("customname", false, ChangeableStage.AUTOMATICSTAGE);
}
