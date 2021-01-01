package net.gir.girsignals.controllers;

import java.util.HashMap;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.gir.girsignals.blocks.SignalHV;
import net.gir.girsignals.items.Linkingtool;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")
public class SignalControllerTileEntity extends TileEntity implements SimpleComponent {

	private BlockPos linkedSignalPosition = null;
	private SignalType signalType;
	private Integer[] listOfSupportedIndicies;
	private Object[] tableOfSupportedSignalTypes;
	
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
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		writeBlockPosToNBT(linkedSignalPosition, compound);
		return super.writeToNBT(compound);
	}

	private void onLink() {
		Block b = world.getBlockState(linkedSignalPosition).getBlock();
		if(b instanceof SignalHV)
			signalType = SignalType.HV_TYPE;
		else
			throw new IllegalArgumentException("Block is not a signal!");
		HashMap<String, Integer> supportedSignaleStates = new HashMap<>();
		signalType.supportedSignalStates.getSupportedSignalStates(world,
				linkedSignalPosition, (IExtendedBlockState)world.getBlockState(linkedSignalPosition), supportedSignaleStates);
		listOfSupportedIndicies = supportedSignaleStates.values().toArray(new Integer[supportedSignaleStates.size()]);
		tableOfSupportedSignalTypes = supportedSignaleStates.entrySet().toArray(new Object[supportedSignaleStates.size()]);
	}
	
	public boolean link(ItemStack stack) {
		if (stack.getItem() instanceof Linkingtool) {
			BlockPos old = linkedSignalPosition;
			boolean flag = (linkedSignalPosition = readBlockPosFromNBT(stack.getTagCompound())) != null
					&& (old == null || !old.equals(linkedSignalPosition));
			if (flag) {
				onLink();
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

	public boolean hasLinkImpl() {
		return linkedSignalPosition != null;
	}

	public void unlink() {
		linkedSignalPosition = null;
		tableOfSupportedSignalTypes = null;
		listOfSupportedIndicies = null;
	}

	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] getSupportedSignalTypes(Context context, Arguments args) {
		return tableOfSupportedSignalTypes;
	}

	public Integer[] getSupportedSignalTypesImpl() {
		if (!hasLinkImpl())
			return new Integer[] {};
		return listOfSupportedIndicies;
	}

	public static boolean find(Integer[] arr, int i) {
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

	public boolean changeSignalImpl(int newSignal, int type) {
		if (!hasLinkImpl() || !find(getSupportedSignalTypesImpl(), type))
			return false;
		IExtendedBlockState oldState = (IExtendedBlockState)world.getBlockState(linkedSignalPosition);
		IBlockState state = signalType.onSignalChange.getNewState(world, linkedSignalPosition, oldState, newSignal, type);
		if (oldState == state)
			return false;
		return world.setBlockState(linkedSignalPosition, state);
	}

	@Callback
	@Optional.Method(modid = "opencomputers")
	public Object[] getSignalType(Context context, Arguments args) {
		return new Object[] { signalType.name };
	}

	public String getSignalTypeImpl() {
		return signalType.name;
	}

	@Override
	public String getComponentName() {
		return "signalcontroller";
	}

}
