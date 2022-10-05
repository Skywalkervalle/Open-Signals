package eu.gir.girsignals.blocks.boards;

import java.util.Map;

import eu.gir.girsignals.EnumSignals.OtherSignal;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraft.client.gui.FontRenderer;

public class SignalOther extends Signal {

    public SignalOther() {
        super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "othersignal").signScale(3.5f).offsetX(-5)
                .offsetY(1.8f).noLink().build());
    }

    public static final SEProperty<OtherSignal> OTHERTYPE = SEProperty.of("othertype",
            OtherSignal.HM, ChangeableStage.GUISTAGE, false);

    @Override
    public boolean canHaveCustomname(final Map<SEProperty<?>, Object> map) {
        return true;
    }

    @Override
    public void renderOverlay(final double x, final double y, final double z,
            final SignalTileEnity te, final FontRenderer font) {
        super.renderOverlay(x, y, z, te, font,
                te.getProperty(OTHERTYPE).filter(OtherSignal.HM::equals).isPresent() ? 2.1f
                        : this.prop.customNameRenderHeight);
    }
}