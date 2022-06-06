package eu.gir.girsignals.signalbox;

import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT1;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT2;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.REQUEST_WAY;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.RESET_WAY;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.fromNBT;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.requestWay;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.enums.EnumPathUsage;
import eu.gir.girsignals.enums.LinkType;
import eu.gir.girsignals.init.GIRBlocks;
import eu.gir.girsignals.tileentitys.IChunkloadable;
import eu.gir.girsignals.tileentitys.RedstoneIOTileEntity;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import eu.gir.girsignals.tileentitys.SyncableTileEntity;
import eu.gir.guilib.ecs.GuiSyncNetwork;
import eu.gir.guilib.ecs.interfaces.ISyncable;
import eu.gir.linkableapi.ILinkableTile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class SignalBoxTileEntity extends SyncableTileEntity
        implements ISyncable, IChunkloadable, ILinkableTile {

    public static final String ERROR_STRING = "error";
    public static final String REMOVE_SIGNAL = "removeSignal";

    private static final String LINKED_POS_LIST = "linkedPos";
    private static final String GUI_TAG = "guiTag";
    private static final String LINK_TYPE = "linkType";

    private final Map<BlockPos, LinkType> linkedBlocks = new HashMap<>();
    private final Map<Point, SignalBoxNode> modeGrid = new HashMap<>();
    private final Map<BlockPos, Signal> signals = new HashMap<>();
    private NBTTagCompound guiTag = new NBTTagCompound();

    private final Map<Point, SignalBoxPathway> startsToPath = new HashMap<>();
    private final Map<Point, SignalBoxPathway> endsToPath = new HashMap<>();
    private final Map<SignalBoxPathway, SignalBoxPathway> previousPathways = new HashMap<>(32);
    private WorldLoadOperations worldLoadOps = new WorldLoadOperations(null);

    @Override
    public void setWorld(final World worldIn) {
        super.setWorld(worldIn);
        worldLoadOps = new WorldLoadOperations(world);
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound) {
        final NBTTagList list = new NBTTagList();
        linkedBlocks.forEach((p, t) -> {
            final NBTTagCompound item = NBTUtil.createPosTag(p);
            item.setString(LINK_TYPE, t.name());
            list.appendTag(item);
        });
        compound.setTag(LINKED_POS_LIST, list);
        compound.setTag(GUI_TAG, guiTag);
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        final NBTTagList list = (NBTTagList) compound.getTag(LINKED_POS_LIST);
        if (list != null) {
            linkedBlocks.clear();
            list.forEach(pos -> {
                final NBTTagCompound item = (NBTTagCompound) pos;
                if (item.hasKey(LINK_TYPE))
                    linkedBlocks.put(NBTUtil.getPosFromTag(item),
                            LinkType.valueOf(item.getString(LINK_TYPE)));
            });
        }
        this.guiTag = compound.getCompoundTag(GUI_TAG);
        this.updateModeGridFromUI();
        super.readFromNBT(compound);
        if (world != null)
            onLoad();
    }

    private void updateModeGridFromUI() {
        modeGrid.clear();
        this.guiTag.getKeySet().forEach(key -> {
            final String[] names = key.split("\\.");
            if (names.length < 2)
                return;
            final int x = Integer.parseInt(names[0]);
            final int y = Integer.parseInt(names[1]);
            final SignalBoxNode node = new SignalBoxNode(new Point(x, y));
            node.read(this.guiTag);
            modeGrid.put(node.getPoint(), node);
        });
    }

    private void sendGuiTag() {
        this.clientSyncs.forEach(ui -> GuiSyncNetwork.sendToClient(guiTag, ui.getPlayer()));
    }

    private void resendSignalTilesToUI() {
        startsToPath.values().forEach(signal -> signal.write(guiTag));
        sendGuiTag();
    }

    private void onWayAdd(final SignalBoxPathway pathway) {
        pathway.setWorld(world);
        startsToPath.put(pathway.getFirstPoint(), pathway);
        endsToPath.put(pathway.getLastPoint(), pathway);
        final SignalBoxPathway next = startsToPath.get(pathway.getLastPoint());
        if (next != null)
            previousPathways.put(next, pathway);
        final SignalBoxPathway previous = endsToPath.get(pathway.getFirstPoint());
        if (previous != null)
            previousPathways.put(pathway, previous);
        pathway.setPathStatus(EnumPathUsage.SELECTED);
        pathway.updatePathwaySignals();
        SignalBoxPathway previousPath = pathway;
        while ((previousPath = previousPathways.get(previousPath)) != null) {
            previousPath.updatePathwaySignals();
        }
        resendSignalTilesToUI();
    }

    @Override
    public void updateTag(final NBTTagCompound compound) {
        if (compound == null)
            return;
        if (compound.hasKey(REMOVE_SIGNAL)) {
            final NBTTagCompound request = (NBTTagCompound) compound.getTag(REMOVE_SIGNAL);
            final BlockPos p1 = NBTUtil.getPosFromTag(request);
            if (signals.containsKey(p1)) {
                signals.remove(p1);
                worldLoadOps.loadAndReset(p1);
            }
            linkedBlocks.remove(p1);
        }
        if (compound.hasKey(RESET_WAY)) {
            final NBTTagCompound request = (NBTTagCompound) compound.getTag(RESET_WAY);
            final Point p1 = fromNBT(request, POINT1);
            final SignalBoxPathway pathway = startsToPath.get(p1);
            if (pathway == null) {
                GirsignalsMain.log.atWarn()
                        .log("Signalboxpath is null, this should not be the case!");
                return;
            }
            pathway.resetPathway();
            resendSignalTilesToUI();
            this.startsToPath.remove(pathway.getFirstPoint());
            this.endsToPath.remove(pathway.getLastPoint());
            this.previousPathways.remove(pathway);
            this.previousPathways.entrySet().removeIf(entry -> entry.getValue().equals(pathway));
            return;
        }
        if (compound.hasKey(REQUEST_WAY)) {
            final NBTTagCompound request = (NBTTagCompound) compound.getTag(REQUEST_WAY);
            final Point p1 = fromNBT(request, POINT1);
            final Point p2 = fromNBT(request, POINT2);
            final Optional<SignalBoxPathway> ways = requestWay(modeGrid, p1, p2);
            if (ways.isPresent()) {
                this.onWayAdd(ways.get());
            } else {
                final NBTTagCompound update = new NBTTagCompound();
                update.setString(ERROR_STRING, "error.nopathfound");
                this.clientSyncs.forEach(ui -> GuiSyncNetwork.sendToClient(update, ui.getPlayer()));
            }
            return;
        }
        this.guiTag = compound;
        this.syncClient();
        updateModeGridFromUI();
    }

    @Override
    public NBTTagCompound getTag() {
        return this.guiTag;
    }

    @Override
    public boolean hasLink() {
        return !linkedBlocks.isEmpty();
    }

    @Override
    public boolean link(final BlockPos linkedPos) {
        if (linkedBlocks.containsKey(linkedPos))
            return false;
        final IBlockState state = world.getBlockState(linkedPos);
        final Block block = state.getBlock();
        LinkType type = LinkType.SIGNAL;
        if (block == GIRBlocks.REDSTONE_IN) {
            type = LinkType.INPUT;
            if (!world.isRemote)
                loadChunkAndGetTile(RedstoneIOTileEntity.class, world, linkedPos,
                        (tile, _u) -> tile.link(this.pos));
        } else if (block == GIRBlocks.REDSTONE_OUT) {
            type = LinkType.OUTPUT;
        }
        if (!world.isRemote) {
            if (type.equals(LinkType.SIGNAL)) {
                loadChunkAndGetTile(SignalTileEnity.class, world, linkedPos, this::updateSingle);
                new WorldLoadOperations(world).loadAndReset(linkedPos);
            }
        }
        linkedBlocks.put(linkedPos, type);
        this.syncClient();
        return true;
    }

    private void updateSingle(final SignalTileEnity signaltile, final Chunk unused) {
        final BlockPos signalPos = signaltile.getPos();
        signals.put(signalPos, signaltile.getSignal());
        syncClient();
    }

    @Override
    public void onLoad() {
        if (world.isRemote)
            return;
        signals.clear();
        new Thread(() -> {
            linkedBlocks.forEach((linkedPos, _u) -> loadChunkAndGetTile(SignalTileEnity.class,
                    world, linkedPos, this::updateSingle));
        }).start();
    }

    @Override
    public boolean unlink() {
        signals.keySet().forEach(worldLoadOps::loadAndReset);
        linkedBlocks.entrySet().stream().filter(entry -> !LinkType.SIGNAL.equals(entry.getValue()))
                .forEach(entry -> {
                    loadChunkAndGetTile(RedstoneIOTileEntity.class, world, entry.getKey(),
                            (tile, _u) -> tile.unlink(pos));
                });
        linkedBlocks.clear();
        signals.clear();
        syncClient();
        return true;
    }

    public Signal getSignal(final BlockPos pos) {
        return this.signals.get(pos);
    }

    public ImmutableMap<BlockPos, LinkType> getPositions() {
        return ImmutableMap.copyOf(this.linkedBlocks);
    }

    public boolean isEmpty() {
        return this.modeGrid.isEmpty();
    }

    public void updateRedstonInput(final BlockPos pos, final boolean power) {
        if (power) {
            startsToPath.values().forEach(pathways -> pathways.tryBlock(pos));
            startsToPath.values().forEach(pathways -> pathways.tryReset(pos));
            resendSignalTilesToUI();
        }
    }

}
