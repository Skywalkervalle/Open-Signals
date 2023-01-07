package com.troblecodings.signals.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.troblecodings.core.MessageWrapper;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.guilib.ecs.interfaces.IIntegerable;
import com.troblecodings.guilib.ecs.interfaces.ITagableItem;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.OSBlocks;
import com.troblecodings.signals.init.OSTabs;
import com.troblecodings.signals.statehandler.SignalStateHandler;
import com.troblecodings.signals.statehandler.SignalStateInfo;
import com.troblecodings.signals.tileentitys.SignalTileEntity;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Placementtool extends BlockItem
        implements IIntegerable<Signal>, ITagableItem, MessageWrapper {

    public static final String BLOCK_TYPE_ID = "blocktypeid";
    public static final String SIGNAL_CUSTOMNAME = "customname";

    public final ArrayList<Signal> signals = new ArrayList<>();

    public Placementtool() {
        super(Blocks.AIR, new Item.Properties().tab(OSTabs.TAB));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level worldIn, final Player player,
            final InteractionHand hand) {
        if (worldIn.isClientSide)
            return InteractionResultHolder.success(player.getItemInHand(hand));
        OpenSignalsMain.handler.invokeGui(Placementtool.class, player, worldIn, player.getOnPos(),
                "placementtool");
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public InteractionResult place(final BlockPlaceContext context) {
        final InteractionResult result = super.place(context);
        final Player player = context.getPlayer();
        final Level worldIn = context.getLevel();
        final NBTWrapper compound = new NBTWrapper(player.getMainHandItem().getOrCreateTag());
        final BlockPos pos = context.getClickedPos();
        if (!compound.contains(BLOCK_TYPE_ID)) {
            if (!worldIn.isClientSide)
                return result;
            translateMessageWrapper(player, "pt.itemnotset");
            return InteractionResult.FAIL;
        }
        if (player.isShiftKeyDown()) {
            if (worldIn.isClientSide())
                return result;
            OpenSignalsMain.handler.invokeGui(Placementtool.class, player, worldIn,
                    player.getOnPos(), "placementtool");
            return result;
        }

        final Signal signal = getObjFromID(compound.getInteger(BLOCK_TYPE_ID));
        final Map<SEProperty, String> states = new HashMap<>();
        final List<SEProperty> properties = signal.getProperties();

        properties.forEach(property -> {
            if (property.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG)) {
                states.put(property, property.getDefault());
                return;
            }
            if (!compound.contains(property.getName())) {
                return;
            }
            if (property.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
                states.put(property,
                        property.getObjFromID(compound.getInteger(property.getName())));
            } else if (property.isChangabelAtStage(ChangeableStage.APISTAGE)
                    && compound.getInteger(property.getName()) == 1) {
                states.put(property, property.getDefault());
            }
        });
        final SignalTileEntity tile = new SignalTileEntity(
                new TileEntityInfo(pos, signal.defaultBlockState()));
        final int height = signal.getHeight(states);
        BlockPos lastPos = context.getClickedPos();
        for (int i = 0; i < height; i++) {
            if (!worldIn.isEmptyBlock(lastPos = lastPos.above())) {
                worldIn.removeBlock(lastPos, true);
                return InteractionResult.FAIL;
            }
        }
        lastPos = context.getClickedPos();
        for (int i = 0; i < height; i++) {
            worldIn.setBlockAndUpdate(lastPos = lastPos.above(),
                    OSBlocks.GHOST_BLOCK.getStateForPlacement(context));
        }

        final String name = compound.getString(SIGNAL_CUSTOMNAME);
        if (name != null && tile != null) {
            tile.setNameWithOutSync(name);
            states.put(Signal.CUSTOMNAME, name);
        }
        worldIn.setBlockAndUpdate(pos, signal.getStateForPlacement(context));
        worldIn.setBlockEntity(tile);
        SignalStateHandler.setStates(new SignalStateInfo(worldIn, pos), states);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        final Player player = context.getPlayer();
        final NBTWrapper compound = new NBTWrapper(player.getMainHandItem().getOrCreateTag());
        if (!compound.contains(BLOCK_TYPE_ID)) {
            translateMessageWrapper(player, "pt.itemnotset");
            return null;
        }
        return this.signals.get(compound.getInteger(BLOCK_TYPE_ID)).getStateForPlacement(context);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public String getNamedObj(final int obj) {
        return I18n.get("property." + this.getName() + ".name") + ": " + this.getObjFromID(obj);
    }

    @Override
    public Signal getObjFromID(final int obj) {
        return signals.get(obj);
    }

    @Override
    public int count() {
        return signals.size();
    }

    @Override
    public String getName() {
        return "signaltype";
    }

    public void addSignal(final Signal signal) {
        this.signals.add(signal);
    }

}
