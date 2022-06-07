package eu.gir.girsignals.test;

import static eu.gir.girsignals.blocks.signals.SignalHL.DISTANTSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalHL.EXITSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalHL.LIGHTBAR;
import static eu.gir.girsignals.blocks.signals.SignalHL.STOPSIGNAL;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HLDistant;
import eu.gir.girsignals.EnumSignals.HLExit;
import eu.gir.girsignals.EnumSignals.HLLightbar;
import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.HPBlock;
import eu.gir.girsignals.EnumSignals.HPHome;
import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KSDistant;
import eu.gir.girsignals.EnumSignals.KSMain;
import eu.gir.girsignals.EnumSignals.VR;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.blocks.signals.SignalKS;
import eu.gir.girsignals.signalbox.config.HLSignalConfig;
import eu.gir.girsignals.signalbox.config.ISignalAutoconfig.ConfigInfo;
import eu.gir.girsignals.test.DummySignal.DummyBuilder;

public class GIRConfigtestHL {

    final HLSignalConfig config = HLSignalConfig.INSTANCE;

    @Test
    public void testHLConfig() {

        // HL -> HL
        configtestHL(HL.HL10, HLExit.HL1, HLDistant.HL10, HLLightbar.OFF, HL.HP0, HLExit.HP0,
                HLDistant.HL10, HLLightbar.OFF, 0);
        configtestHL(HL.HL1, HLExit.HL1, HLDistant.HL1, HLLightbar.OFF, HL.HL10, HLExit.HL1,
                HLDistant.HL1, HLLightbar.OFF, 0);
        configtestHL(HL.HL11_12, HLExit.HL2_3, HLDistant.HL10, HLLightbar.OFF, HL.HP0, HLExit.HP0,
                HLDistant.HL10, HLLightbar.OFF, 4);
        configtestHL(HL.HL11_12, HLExit.HL2_3, HLDistant.HL10, HLLightbar.YELLOW, HL.HP0,
                HLExit.HP0, HLDistant.HL10, HLLightbar.OFF, 6);
        configtestHL(HL.HL11_12, HLExit.HL2_3, HLDistant.HL10, HLLightbar.GREEN, HL.HP0, HLExit.HP0,
                HLDistant.HL10, HLLightbar.OFF, 10);

        // HL -> KS
        configtestHL_KS(HL.HL10, HLExit.HL1, HLDistant.HL10, HLLightbar.OFF, KS.HP0, KSMain.HP0,
                KSDistant.KS2, ZS32.OFF, ZS32.OFF, 0);

        // HL -> HV
        configtestHL_HV(HL.HL10, HLExit.HL1, HLDistant.HL10, HLLightbar.OFF, HP.HP0, HPHome.HP0,
                HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF, 0);
    }

    private void configtestHL(final HL hlcurrent, final HLExit exitcurrent,
            final HLDistant distantcurrent, final HLLightbar lightcurrent, final HL hlnext,
            final HLExit exitnext, final HLDistant distantnext, final HLLightbar lightnext,
            final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, HL.HP0)
                .of(EXITSIGNAL, HLExit.HP0).of(DISTANTSIGNAL, HLDistant.HL10)
                .of(LIGHTBAR, HLLightbar.OFF).build();
        final DummySignal DummySignal = signalBase.copy();
        config.reset(DummySignal);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, hlcurrent)
                .of(EXITSIGNAL, exitcurrent).of(DISTANTSIGNAL, distantcurrent)
                .of(LIGHTBAR, lightcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(STOPSIGNAL, hlnext)
                .of(EXITSIGNAL, exitnext).of(DISTANTSIGNAL, distantnext).of(LIGHTBAR, lightnext)
                .build();
        config.change(new ConfigInfo(DummySignal, signalnext, speed));
        assertEquals(signalDummy, DummySignal);
    }

    private void configtestHL_KS(final HL hlcurrent, final HLExit exitcurrent,
            final HLDistant distantcurrent, final HLLightbar lightcurrent, final KS ksnext,
            final KSMain ksmainnext, final KSDistant distantnext, final ZS32 zs3next,
            final ZS32 zs3vnext, final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, HL.HP0)
                .of(EXITSIGNAL, HLExit.HP0).of(DISTANTSIGNAL, HLDistant.HL10)
                .of(LIGHTBAR, HLLightbar.OFF).build();
        final DummySignal DummySignal = signalBase.copy();
        config.reset(DummySignal);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, hlcurrent)
                .of(EXITSIGNAL, exitcurrent).of(DISTANTSIGNAL, distantcurrent)
                .of(LIGHTBAR, lightcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(SignalKS.STOPSIGNAL, ksnext)
                .of(SignalKS.MAINSIGNAL, ksmainnext).of(SignalKS.DISTANTSIGNAL, distantnext)
                .of(SignalKS.ZS3, zs3next).of(SignalKS.ZS3V, zs3vnext).build();
        config.change(new ConfigInfo(DummySignal, signalnext, speed));
        assertEquals(signalDummy, DummySignal);
    }

    private void configtestHL_HV(final HL hlcurrent, final HLExit exitcurrent,
            final HLDistant distantcurrent, final HLLightbar lightcurrent, final HP hpnext,
            final HPHome hphomenext, final HPBlock hpblocknext, final VR vrnext, final ZS32 zs3next,
            final ZS32 zs3vnext, final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, HL.HP0)
                .of(EXITSIGNAL, HLExit.HP0).of(DISTANTSIGNAL, HLDistant.HL10)
                .of(LIGHTBAR, HLLightbar.OFF).build();
        final DummySignal DummySignal = signalBase.copy();
        config.reset(DummySignal);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, hlcurrent)
                .of(EXITSIGNAL, exitcurrent).of(DISTANTSIGNAL, distantcurrent)
                .of(LIGHTBAR, lightcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(SignalHV.STOPSIGNAL, hpnext)
                .of(SignalHV.HPHOME, hphomenext).of(SignalHV.HPBLOCK, hpblocknext)
                .of(SignalHV.DISTANTSIGNAL, vrnext).of(SignalHV.ZS3, zs3next)
                .of(SignalHV.ZS3V, zs3vnext).build();
        config.change(new ConfigInfo(DummySignal, signalnext, speed));
        assertEquals(signalDummy, DummySignal);
    }
}
