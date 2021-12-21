package eu.gir.girsignals.guis;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import eu.gir.girsignals.EnumSignals.EnumMode;
import eu.gir.girsignals.EnumSignals.EnumMuxMode;
import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.guis.guilib.DrawUtil;
import eu.gir.girsignals.guis.guilib.DrawUtil.EnumIntegerable;
import eu.gir.girsignals.guis.guilib.DrawUtil.SizeIntegerables;
import eu.gir.girsignals.guis.guilib.GuiBase;
import eu.gir.girsignals.guis.guilib.GuiElements;
import eu.gir.girsignals.guis.guilib.IIntegerable;
import eu.gir.girsignals.guis.guilib.UIEntity;
import eu.gir.girsignals.init.GIRNetworkHandler;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity.EnumRedstoneMode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class GuiSignalController extends GuiBase {

	private final BlockPos pos;
	private BlockModelShapes manager;
	private ThreadLocal<BufferBuilder> model = ThreadLocal.withInitial(() -> new BufferBuilder(500));
	public ContainerSignalController sigController;

	public GuiSignalController(final SignalControllerTileEntity entity) {
		super("TestTitle");
		this.sigController = new ContainerSignalController(entity, this);
		this.pos = entity.getPos();
	}

	private HashMap<SEProperty<?>, Object> createMap(Signal signal) {
		final HashMap<SEProperty<?>, Object> map = Maps.newHashMap();
		for (Entry<Integer, Integer> entry : sigController.guiCacheList) {
			final SEProperty<?> prop = SEProperty.cst(signal.getPropertyFromID(entry.getKey()));
			map.put(prop, prop.getObjFromID(entry.getValue()));
		}

		for (int i = 0; i < sigController.supportedSigTypes.length; i++) {
			final SEProperty<?> prop = SEProperty.cst(signal.getPropertyFromID(sigController.supportedSigTypes[i]));
			int sigState = sigController.supportedSigStates[i];
			if (sigState < 0 || sigState >= prop.count())
				sigState = 0;
			map.put(prop, prop.getObjFromID(sigState));
		}
		return map;
	}

	private void addManuellMode(final Signal signal) {
		HashMap<SEProperty<?>, Object> map = createMap(signal);
		for (SEProperty<?> entry : map.keySet()) {
			of(entry, map, n -> {
			});

		}
	}

	private void of(SEProperty<?> prop, HashMap<SEProperty<?>, Object> map, IntConsumer consumer) {
		if (prop.test(map) && (prop.isChangabelAtStage(ChangeableStage.APISTAGE)
				|| prop.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG))) {
			UIEntity guiEnum = GuiElements.createEnumElement(prop, consumer);
			entity.add(guiEnum);
		}
	}

	private void addSingleMode(final Signal signal) {
		final int faceInt = sigController.faceUsed.ordinal();
		UIEntity facingButton = GuiElements.createEnumElement(new EnumIntegerable<>(EnumFacing.class), m -> {
			sigController.faceUsed = EnumFacing.values()[m];
		});
		entity.add(facingButton);

		final int fLen = sigController.supportedSigTypes.length;
		final IIntegerable<?> possibleSignalTypesIntegerable = new SizeIntegerables<String>("sigtype", fLen + 1, in -> {
			if (in >= fLen)
				return "None";
			final int type = sigController.supportedSigTypes[in];
			final String name = signal.getPropertyFromID(type).getName();
			return name;
		}) {
			@Override
			public String getNamedObj(int obj) {
				return I18n.format("property." + this.getName() + ".name") + ": " + getObjFromID(obj).toUpperCase();
			}
		};
		final int config = sigController.facingRedstoneModes[faceInt];
		final int[] unpacked = SignalControllerTileEntity.unpack(config);
		final int sigType = unpacked[0];
		final int sigOn = unpacked[1];
		final int sigOff = unpacked[2];
		UIEntity possibleSignalTypes = GuiElements.createEnumElement(possibleSignalTypesIntegerable,
				in -> sendPacked(pack(in, sigOn, sigOff)));
		entity.add(possibleSignalTypes);

		if (sigType < sigController.supportedSigTypes.length) {
			final SEProperty<?> prop = SEProperty
					.cst(signal.getPropertyFromID(sigController.supportedSigTypes[sigType]));
			HashMap<SEProperty<?>, Object> map = createMap(signal);
			of(prop, map, in -> sendPacked(pack(sigType, in, sigOff)));
			of(prop, map, in -> sendPacked(pack(sigType, sigOn, in)));
		}
	}

	private void addMUXMode() {
		UIEntity muxModeButton = GuiElements.createEnumElement(new EnumIntegerable<>(EnumMuxMode.class), in -> {
			sigController.muxMode = EnumMuxMode.values()[in];
			for (int i = 0; i < sigController.facingRedstoneModes.length; i++) {
				if (sigController.facingRedstoneModes[i] == in) {
					sigController.faceUsed = EnumFacing.values()[i];
					break;
				}
			}
		});
		entity.add(muxModeButton);
		UIEntity muxModeFacing = GuiElements.createEnumElement(new EnumIntegerable<>(EnumFacing.class), in -> {
			sigController.faceUsed = EnumFacing.values()[in];
			for (int i = 0; i < sigController.facingRedstoneModes.length; i++) {
				if (sigController.facingRedstoneModes[i] == sigController.muxMode.ordinal()) {
					final int idx = i;
					sendToPos(GIRNetworkHandler.SIG_CON_RS_FACING_UPDATE_SET, bf -> {
						bf.writeInt(idx);
						bf.writeInt(3);
					});
				}
			}
			sendToPos(GIRNetworkHandler.SIG_CON_RS_FACING_UPDATE_SET, bf -> {
				bf.writeInt(in);
				bf.writeInt(sigController.muxMode.ordinal());
			});
		});
		entity.add(muxModeFacing);

	}

	private void addRSMode(final Signal signal) {
		UIEntity rsModeRedstone = GuiElements.createEnumElement(new EnumIntegerable<>(EnumRedstoneMode.class), in -> {
			sigController.rsMode = EnumRedstoneMode.values()[in];
			sendToPos(GIRNetworkHandler.SIG_CON_RS_SET, buffer -> {
				buffer.writeInt(in);
			});
		});
		entity.add(rsModeRedstone);
		if (sigController.rsMode == EnumRedstoneMode.SINGLE) {
			addSingleMode(signal);
		} else if (sigController.rsMode == EnumRedstoneMode.MUX) {
			addMUXMode();
		}
	}

	@Override
	public void initGui() {
		this.mc.player.openContainer = this.sigController;
		this.manager = this.mc.getBlockRendererDispatcher().getBlockModelShapes();
		super.initGui();
		updateDraw();
		if (sigController.signalType < 0 || !sigController.hasLink) {
			buttonList.clear();
			return;
		}
		synchronized (buttonList) {
			final Signal signal = Signal.SIGNALLIST.get(sigController.signalType);

			final EnumIntegerable<EnumMode> modeIntegerable = new EnumIntegerable<>(EnumMode.class);
			UIEntity settingsButton = GuiElements.createEnumElement(modeIntegerable, input -> {
				sigController.indexMode = EnumMode.values()[input];
				sigController.indexCurrentlyUsed = 0;
			});
			entity.add(settingsButton);
			if (EnumMode.MANUELL == sigController.indexMode) {
				addManuellMode(signal);
			} else if (EnumMode.REDSTONE == sigController.indexMode) {
				addRSMode(signal);
			}
		}
	}

	public void sendPacked(final int packed) {
		sendToPos(GIRNetworkHandler.SIG_CON_RS_FACING_UPDATE_SET, buffer -> {
			buffer.writeInt(sigController.faceUsed.ordinal());
			buffer.writeInt(packed);
		});
	}

	public int pack(final int sigTypeID, final int onSig, final int offSig) {
		return (sigTypeID & 0b00000000000000000000000000001111) | ((onSig & 0b00000000000000000000000000111111) << 4)
				| ((offSig & 0b00000000000000000000000000111111) << 10);
	}

	@Override
	public void onGuiClosed() {
		sendToPos(GIRNetworkHandler.SIG_CON_SAVE_UI_STATE, buf -> {
			buf.writeInt(sigController.indexMode.ordinal());
			buf.writeInt(sigController.faceUsed.ordinal());
			buf.writeInt(sigController.indexCurrentlyUsed);
			buf.writeInt(sigController.muxMode.ordinal());
		});
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!sigController.hasLink) {
			final String s = "No Signal connected!";
			final int width = mc.fontRenderer.getStringWidth(s);
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft + (this.xSize - width * 2) / 2,
					this.guiTop + (this.ySize - mc.fontRenderer.FONT_HEIGHT) / 2 - 20, 0);
			GlStateManager.scale(2, 2, 2);
			mc.fontRenderer.drawStringWithShadow(s, 0, 0, 0xFFFF0000);
			GlStateManager.popMatrix();
			return;
		}

		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.enableRescaleNormal();
		GlStateManager.pushMatrix();
		GlStateManager.translate(this.guiLeft + this.xSize - 70, this.guiTop + this.ySize / 2, 100.0f);
		GlStateManager.rotate(180, 0, 1, 0);
		GlStateManager.scale(22.0F, -22.0F, 22.0F);
		GlStateManager.translate(-0.5f, -3.5f, -0.5f);
		DrawUtil.draw(model.get());
		GlStateManager.popMatrix();
		GlStateManager.disableRescaleNormal();
	}

	@Override
	public String getTitle() {
		if (sigController.signalType < 0 || !sigController.hasLink)
			return "";
		final Signal signal = Signal.SIGNALLIST.get(sigController.signalType);
		return I18n.format("tile." + signal.getRegistryName().getResourcePath() + ".name")
				+ (sigController.entity.hasCustomName() ? " - " + sigController.entity.getName() : "");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void updateDraw() {
		if (sigController.supportedSigStates == null || !sigController.hasLink)
			return;
		final Signal signal = Signal.SIGNALLIST.get(sigController.signalType);
		IExtendedBlockState ebs = (IExtendedBlockState) signal.getDefaultState();

		for (Entry<Integer, Integer> entry : sigController.guiCacheList) {
			SEProperty prop = SEProperty.cst(signal.getPropertyFromID(entry.getKey()));
			ebs = ebs.withProperty(prop, prop.getObjFromID(entry.getValue()));
		}

		for (int i = 0; i < sigController.supportedSigStates.length; i++) {
			int sigState = sigController.supportedSigStates[i];
			SEProperty prop = SEProperty.cst(signal.getPropertyFromID(sigController.supportedSigTypes[i]));
			if (sigState < 0 || sigState >= prop.count())
				continue;
			ebs = ebs.withProperty(prop, prop.getObjFromID(sigState));
		}
		model.get().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		DrawUtil.addToBuffer(model.get(), manager, ebs);
		model.get().finishDrawing();
	}

	public void sendChanges(final int id, final int data) {
		sendToPos(GIRNetworkHandler.SIG_CON_GUI_MANUELL_SET, buffer -> {
			buffer.writeInt(id);
			buffer.writeInt(data);
		});
	}

	private void sendToPos(final byte id, final Consumer<ByteBuf> consumer) {
		ByteBuf buffer = Unpooled.buffer();
		buffer.writeByte(id);
		buffer.writeInt(pos.getX());
		buffer.writeInt(pos.getY());
		buffer.writeInt(pos.getZ());
		consumer.accept(buffer);
		CPacketCustomPayload payload = new CPacketCustomPayload(GIRNetworkHandler.CHANNELNAME,
				new PacketBuffer(buffer));
		GirsignalsMain.PROXY.CHANNEL.sendToServer(new FMLProxyPacket(payload));
	}
}
