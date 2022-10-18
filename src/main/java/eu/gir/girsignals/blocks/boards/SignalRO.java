package eu.gir.girsignals.blocks.boards;

import eu.gir.girsignals.EnumSignals.RO;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.init.GIRSounds;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignalRO extends Signal {

    public SignalRO() {
        super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "rosignal").height(1).build());
    }

    public static final SEProperty<RO> RO_TYPE = SEProperty.of("rotype", RO.RO4,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> ATWS = SEProperty.of("atws", false,
            ChangeableStage.APISTAGE_NONE_CONFIG, true, check(RO_TYPE, RO.ATWS));

    public boolean checkDoesSound(final IBlockState state, final World world, final BlockPos pos) {
        final TileEntity tile = world.getTileEntity(pos);

        if (!(tile instanceof SignalTileEnity))
            return false;
        final SignalTileEnity signalTE = (SignalTileEnity) tile;
        return (signalTE.getProperty(ATWS).filter(atws -> (Boolean) atws).isPresent());
    }

    @Override
    public void getUpdate(final World world, final BlockPos pos) {
        if (checkDoesSound(world.getBlockState(pos), world, pos)) {
            world.playSound(null, pos, GIRSounds.rottenwarn, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
    }
}
