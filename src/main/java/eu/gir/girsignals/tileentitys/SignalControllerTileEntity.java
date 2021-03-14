package eu.gir.girsignals.tileentitys;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.debug.NetworkDebug;
import eu.gir.girsignals.items.Linkingtool;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class SignalControllerTileEntity extends TileEntity implements SimpleComponent {

	private BlockPos linkedSignalPosition = null;
	private int[] listOfSupportedIndicies;
	private Map<String, Integer> tableOfSupportedSignalTypes;

	private static final String ID_X = "xLinkedPos";
	private static final String ID_Y = "yLinkedPos";
	private static final String ID_Z = "zLinkedPos";

	public static BlockPos readBlockPosFromNBT(NBTTagCompound compound) {
		if (compound != null && compound.hasKey(ID_X) && compound.hasKey(ID_Y) && compound.hasKey(ID_Z)) {
			return new BlockPos(compound.getInteger(ID_X), compound.getInteger(ID_Y), compound.getInteger(ID_Z));
		}
		return null;
	}

	public static void writeBlockPosToNBT(BlockPos pos, NBTTagCompound compound) {
		if (pos != null && compound != null) {
			compound.setInteger(ID_X, pos.getX());
			compound.setInteger(ID_Y, pos.getY());
			compound.setInteger(ID_Z, pos.getZ());
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		linkedSignalPosition = readBlockPosFromNBT(compound);
		super.readFromNBT(compound);
		if (world != null && world.isRemote && linkedSignalPosition != null)
			onLink();
		NetworkDebug.networkReadHook(compound, world, this);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		writeBlockPosToNBT(linkedSignalPosition, compound);
		super.writeToNBT(compound);
		NetworkDebug.networkWriteHook(compound, world, this);
		return compound;
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.getNbtCompound());
		if (hasLinkImpl())
			onLink();
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	public void onLink() {
		loadChunkAndGetTile((sigtile, ch) -> {
			Signal b = Signal.SIGNALLIST.get(sigtile.getBlockID());

			HashMap<String, Integer> supportedSignaleStates = new HashMap<>();
			sigtile.accumulate((bs, prop, obj) -> {
				if (prop instanceof SEProperty && obj != null) {
					SEProperty<?> p = ((SEProperty<?>) prop);
					if (p.isChangabelAtStage(ChangeableStage.APISTAGE)
							|| p.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG))
						supportedSignaleStates.put(prop.getName(), b.getIDFromProperty(prop));
				}
				return null;
			}, null);
			listOfSupportedIndicies = supportedSignaleStates.values().stream().mapToInt(Integer::intValue).toArray();
			tableOfSupportedSignalTypes = supportedSignaleStates;
		});
	}

	@Override
	public void onLoad() {
		if (linkedSignalPosition != null) {
			onLink();
			IBlockState state = world.getBlockState(pos);
			this.world.notifyBlockUpdate(pos, state, state, 3);
		}
	}

	public boolean link(ItemStack stack) {
		if (stack.getItem() instanceof Linkingtool) {
			BlockPos old = linkedSignalPosition;
			boolean flag = (linkedSignalPosition = readBlockPosFromNBT(stack.getTagCompound())) != null
					&& (old == null || !old.equals(linkedSignalPosition));
			if (flag) {
				onLink();
				IBlockState state = world.getBlockState(pos);
				this.world.notifyBlockUpdate(pos, state, state, 3);
			}
			return flag;
		}
		return false;
	}

	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] hasLink(Context context, Arguments args) {
		return new Object[] { hasLinkImpl() };
	}

	public boolean loadChunkAndGetTile(BiConsumer<SignalTileEnity, Chunk> consumer) {
		TileEntity entity = null;
		if(linkedSignalPosition == null)
			return false;
		Chunk ch = world.getChunkFromBlockCoords(linkedSignalPosition);
		boolean flag = !ch.isLoaded();
		if (flag) {
			if (world.isRemote) {
				ChunkProviderClient client = (ChunkProviderClient) world.getChunkProvider();
				ch = client.loadChunk(ch.x, ch.z);
			} else {
				ChunkProviderServer server = (ChunkProviderServer) world.getChunkProvider();
				ch = server.loadChunk(ch.x, ch.z);
			}
		}
		if (ch == null)
			return false;
		entity = ch.getTileEntity(linkedSignalPosition, EnumCreateEntityType.IMMEDIATE);
		boolean flag2 = entity instanceof SignalTileEnity;
		if (flag2)
			consumer.accept((SignalTileEnity) entity, ch);

		if (flag) {
			if (world.isRemote) {
				ChunkProviderClient client = (ChunkProviderClient) world.getChunkProvider();
				client.unloadChunk(ch.x, ch.z);
			} else {
				ChunkProviderServer server = (ChunkProviderServer) world.getChunkProvider();
				server.queueUnload(ch);
			}
		}
		return flag2;
	}

	public boolean hasLinkImpl() {
		if (linkedSignalPosition == null)
			return false;
		if (loadChunkAndGetTile((x,y) -> {}))
			return true;
		if (!world.isRemote)
			unlink();
		return false;
	}

	public void unlink() {
		linkedSignalPosition = null;
		tableOfSupportedSignalTypes = null;
		listOfSupportedIndicies = null;
	}

	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] getSupportedSignalTypes(Context context, Arguments args) {
		return new Object[] { tableOfSupportedSignalTypes };
	}

	public int[] getSupportedSignalTypesImpl() {
		return listOfSupportedIndicies;
	}

	public static boolean find(int[] arr, int i) {
		for (int x : arr)
			if (x == i)
				return true;
		return false;
	}

	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] changeSignal(Context context, Arguments args) {
		return new Object[] { changeSignalImpl(args.checkInteger(0), args.checkInteger(1)) };
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean changeSignalImpl(int type, int newSignal) {
		if (!find(getSupportedSignalTypesImpl(), type))
			return false;
		loadChunkAndGetTile((tile, chunk) -> {
			Signal block = (Signal) Signal.SIGNALLIST.get(tile.getBlockID());
			SEProperty prop = SEProperty.cst(block.getPropertyFromID(type));
			tile.setProperty(prop, prop.getObjFromID(newSignal));
			IBlockState state = chunk.getBlockState(linkedSignalPosition);
			world.markAndNotifyBlock(linkedSignalPosition, chunk, state, state, 3);
		});
		return true;
	}

	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] getSignalType(Context context, Arguments args) {
		return new Object[] { getSignalTypeImpl() };
	}

	private String signalTypeCache = null;

	public String getSignalTypeImpl() {
		if (signalTypeCache == null)
			loadChunkAndGetTile((tile, ch) -> signalTypeCache = Signal.SIGNALLIST.get(tile.getBlockID()).getSignalTypeName());
		return signalTypeCache;
	}

	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] getSignalState(Context context, Arguments args) {
		return new Object[] { getSignalStateImpl(args.checkInteger(0)) };
	}

	@SuppressWarnings("rawtypes")
	public int getSignalStateImpl(int type) {
		if (!find(getSupportedSignalTypesImpl(), type))
			return -1;
		AtomicReference<SignalTileEnity> entity = new AtomicReference<SignalTileEnity>();
		loadChunkAndGetTile((sig, ch) -> entity.set(sig));
		SignalTileEnity tile = entity.get();
		if(tile == null)
			return -1;
		Signal block = (Signal) Signal.SIGNALLIST.get(tile.getBlockID());
		SEProperty prop = SEProperty.cst(block.getPropertyFromID(type));
		java.util.Optional bool = tile.getProperty(prop);
		if (bool.isPresent())
			return SEProperty.getIDFromObj(bool.get());
		return -1;
	}

	public BlockPos getLinkedPosition() {
		return linkedSignalPosition;
	}

	@Override
	public String getComponentName() {
		return "signalcontroller";
	}

}
