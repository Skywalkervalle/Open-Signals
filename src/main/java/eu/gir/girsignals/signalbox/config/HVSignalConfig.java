package eu.gir.girsignals.signalbox.config;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.Lists;

import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HLExit;
import eu.gir.girsignals.EnumSignals.HLLightbar;
import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.HPBlock;
import eu.gir.girsignals.EnumSignals.HPHome;
import eu.gir.girsignals.EnumSignals.HPType;
import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KSDistant;
import eu.gir.girsignals.EnumSignals.VR;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.signals.SignalHL;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.blocks.signals.SignalKS;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

public final class HVSignalConfig implements ISignalAutoconfig {

    public static final HVSignalConfig INSTANCE = new HVSignalConfig();

    private HVSignalConfig() {
    }

    private VR next(final HP hp) {
        switch (hp) {
            case HP0:
                return VR.VR0;
            case HP1:
                return VR.VR1;
            case HP2:
                return VR.VR2;
            case OFF:
            default:
                return VR.OFF;
        }
    }

    private VR next(final HPHome hp) {
        switch (hp) {
            case HP0:
            case HP0_ALTERNATE_RED:
                return VR.VR0;
            case HP1:
                return VR.VR1;
            case HP2:
                return VR.VR2;
            case OFF:
            default:
                return VR.OFF;
        }
    }

    private VR next(final HPBlock hp) {
        switch (hp) {
            case HP0:
                return VR.VR0;
            case HP1:
                return VR.VR1;
            case OFF:
            default:
                return VR.OFF;
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void change(final int speed, final SignalTileEnity current, final SignalTileEnity next) {
        final HashMap<SEProperty, Object> values = new HashMap<>();
        if (next != null) {
            if (speed < 7 && speed > 0 && speed != 4) {
                current.getProperty(SignalHV.ZS3).ifPresent(_u -> {
                    final ZS32 zs32 = ZS32.values()[ZS32.Z.ordinal() + speed];
                    current.setProperty(SignalHV.ZS3, zs32);
                });

                values.put(SignalHV.HPBLOCK, HPBlock.HP1);
                values.put(SignalHV.HPHOME, HPHome.HP2);
                values.put(SignalHV.STOPSIGNAL, HP.HP2);
            } else if (speed == 4) {
                values.put(SignalHV.HPBLOCK, HPBlock.HP1);
                values.put(SignalHV.HPHOME, HPHome.HP2);
                values.put(SignalHV.STOPSIGNAL, HP.HP2);
            } else if (speed >= 7 && speed <= 16) {
                current.getProperty(SignalHV.ZS3).ifPresent(_u -> {
                    final ZS32 zs32 = ZS32.values()[ZS32.Z.ordinal() + speed];
                    current.setProperty(SignalHV.ZS3, zs32);
                });
                values.put(SignalHV.HPBLOCK, HPBlock.HP1);
                values.put(SignalHV.HPHOME, HPHome.HP1);
                values.put(SignalHV.STOPSIGNAL, HP.HP1);
            } else {
                values.put(SignalHV.HPBLOCK, HPBlock.HP1);
                values.put(SignalHV.HPHOME, HPHome.HP1);
                values.put(SignalHV.STOPSIGNAL, HP.HP1);
            }

            final ArrayList<HL> stophlmain = Lists.newArrayList(HL.HP0, HL.HP0_ALTERNATE_RED,
                    HL.HL_SHUNTING, HL.HL_ZS1);
            final ArrayList<HLExit> stophlexit = Lists.newArrayList(HLExit.HP0,
                    HLExit.HP0_ALTERNATE_RED, HLExit.HL_SHUNTING, HLExit.HL_ZS1);
            final ArrayList<HL> hl40main = Lists.newArrayList(HL.HL2_3, HL.HL5_6, HL.HL8_9,
                    HL.HL11_12);
            final boolean hlstop = next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(a -> stophlmain.contains(a)).isPresent()
                    || next.getProperty(SignalHL.EXITSIGNAL).filter(d -> stophlexit.contains(d))
                            .isPresent();
            final boolean hlmain40 = next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(c -> hl40main.contains(c)).isPresent()
                    || next.getProperty(SignalHL.EXITSIGNAL).filter(HLExit.HL2_3::equals)
                            .isPresent();

            current.getProperty(SignalHV.DISTANTSIGNAL)
                    .ifPresent(_u -> next.getProperty(SignalHV.HPTYPE).ifPresent(type -> {
                        VR vr = VR.VR0;
                        switch ((HPType) type) {
                            case HPBLOCK:
                                vr = next((HPBlock) next.getProperty(SignalHV.HPBLOCK).get());
                                break;
                            case HPHOME:
                                vr = next((HPHome) next.getProperty(SignalHV.HPHOME).get());
                                break;
                            case STOPSIGNAL:
                                vr = next((HP) next.getProperty(SignalHV.STOPSIGNAL).get());
                                break;
                            case OFF:
                            default:
                                break;
                        }
                        current.setProperty(SignalHV.DISTANTSIGNAL, vr);
                    }));
            current.getProperty(SignalHV.ZS3V).ifPresent(_u -> {
                current.setProperty(SignalHV.ZS3V, ZS32.OFF);
                next.getProperty(SignalHV.ZS3)
                        .ifPresent(prevzs3 -> current.setProperty(SignalHV.ZS3V, (ZS32) prevzs3));
            });

            if (next.getProperty(SignalHL.STOPSIGNAL).isPresent()
                    || next.getProperty(SignalHL.EXITSIGNAL).isPresent()) {
                if (current.getProperty(SignalHV.DISTANTSIGNAL).isPresent()) {
                    if (hlstop) {
                        current.setProperty(SignalHV.DISTANTSIGNAL, VR.VR0);
                    } else if (current.getProperty(SignalHV.ZS3V).isPresent()) {
                        if (next.getProperty(SignalHL.STOPSIGNAL).isPresent()
                                || next.getProperty(SignalHL.EXITSIGNAL).isPresent()) {
                            if (hlmain40 && next.getProperty(SignalHL.LIGHTBAR)
                                    .filter(HLLightbar.OFF::equals).isPresent()) {
                                values.put(SignalHV.DISTANTSIGNAL, VR.VR2);
                            } else if (hlmain40 && next.getProperty(SignalHL.LIGHTBAR)
                                    .filter(HLLightbar.YELLOW::equals).isPresent()) {
                                values.put(SignalHV.DISTANTSIGNAL, VR.VR2);
                                values.put(SignalHV.ZS3V, ZS32.Z6);
                            } else if (hlmain40 && next.getProperty(SignalHL.LIGHTBAR)
                                    .filter(HLLightbar.GREEN::equals).isPresent()) {
                                values.put(SignalHV.DISTANTSIGNAL, VR.VR1);
                                values.put(SignalHV.ZS3V, ZS32.Z10);
                            } else if (hlstop) {
                                values.put(SignalHV.DISTANTSIGNAL, VR.VR0);
                            } else {
                                current.setProperty(SignalHV.DISTANTSIGNAL, VR.VR1);
                            }
                        }
                    } else if (hlmain40) {
                        current.setProperty(SignalHV.DISTANTSIGNAL, VR.VR2);
                    } else {
                        current.setProperty(SignalHV.DISTANTSIGNAL, VR.VR1);
                    }

                }

                if (hlmain40 && next.getProperty(SignalHL.LIGHTBAR).filter(HLLightbar.OFF::equals)
                        .isPresent()) {
                    values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1_BLINK);
                    values.put(SignalKS.ZS3V, ZS32.Z4);
                } else if (hlmain40 && next.getProperty(SignalHL.LIGHTBAR)
                        .filter(HLLightbar.YELLOW::equals).isPresent()) {
                    values.put(SignalKS.STOPSIGNAL, KS.KS1_BLINK);
                    values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1_BLINK);
                    values.put(SignalKS.ZS3V, ZS32.Z6);
                } else if (hlmain40 && next.getProperty(SignalHL.LIGHTBAR)
                        .filter(HLLightbar.GREEN::equals).isPresent()) {
                    values.put(SignalKS.STOPSIGNAL, KS.KS1_BLINK);
                    values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1_BLINK);
                    values.put(SignalKS.ZS3V, ZS32.Z10);
                } else if (hlstop) {
                    values.put(SignalKS.STOPSIGNAL, KS.KS2);
                    values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS2);
                } else {
                    values.put(SignalKS.STOPSIGNAL, KS.KS1);
                    values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1);
                }
            }
        } else {
            values.put(SignalHV.HPBLOCK, HPBlock.HP1);
            values.put(SignalHV.HPHOME, HPHome.HP2);
            values.put(SignalHV.STOPSIGNAL, HP.HP2);
            values.put(SignalHV.DISTANTSIGNAL, VR.VR0);
            values.put(SignalHV.ZS3, ZS32.OFF);
            values.put(SignalHV.ZS3V, ZS32.OFF);
            values.put(SignalHV.ZS1, false);
            values.put(SignalHV.ZS7, false);
        }
        this.changeIfPresent(values, current);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void reset(final SignalTileEnity current) {
        final HashMap<SEProperty, Object> values = new HashMap<>();
        values.put(SignalHV.HPBLOCK, HPBlock.HP0);
        values.put(SignalHV.HPHOME, HPHome.HP0);
        values.put(SignalHV.STOPSIGNAL, HP.HP0);
        values.put(SignalHV.DISTANTSIGNAL, VR.VR0);
        values.put(SignalHV.ZS3, ZS32.OFF);
        values.put(SignalHV.ZS3V, ZS32.OFF);
        values.put(SignalHV.ZS1, false);
        values.put(SignalHV.ZS7, false);
        this.changeIfPresent(values, current);
    }

}
