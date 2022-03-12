package eu.gir.girsignals.guis;

import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT1;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT2;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.RESET_WAY;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.toNBT;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;

import eu.gir.girsignals.signalbox.EnumGuiMode;
import eu.gir.girsignals.signalbox.PathOption;
import eu.gir.girsignals.signalbox.PathOption.EnumPathUsage;
import eu.gir.girsignals.signalbox.Point;
import eu.gir.girsignals.signalbox.SignalBoxTileEntity;
import eu.gir.girsignals.signalbox.SignalBoxUtil;
import eu.gir.girsignals.signalbox.SignalNode;
import eu.gir.guilib.ecs.DrawUtil.DisableIntegerable;
import eu.gir.guilib.ecs.DrawUtil.SizeIntegerables;
import eu.gir.guilib.ecs.GuiBase;
import eu.gir.guilib.ecs.GuiElements;
import eu.gir.guilib.ecs.GuiSyncNetwork;
import eu.gir.guilib.ecs.entitys.UIBox;
import eu.gir.guilib.ecs.entitys.UIEntity;
import eu.gir.guilib.ecs.entitys.UIEnumerable;
import eu.gir.guilib.ecs.entitys.UIStack;
import eu.gir.guilib.ecs.entitys.input.UIClickable;
import eu.gir.guilib.ecs.entitys.input.UIDrag;
import eu.gir.guilib.ecs.entitys.input.UIScroll;
import eu.gir.guilib.ecs.entitys.render.UIBorder;
import eu.gir.guilib.ecs.entitys.render.UIColor;
import eu.gir.guilib.ecs.entitys.render.UILabel;
import eu.gir.guilib.ecs.entitys.render.UIScissor;
import eu.gir.guilib.ecs.entitys.render.UIToolTip;
import eu.gir.guilib.ecs.entitys.transform.UIScale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class GuiSignalBox extends GuiBase {
	
	private static final int SELECTION_COLOR = 0x2900FF00;
	private static final int BACKGROUND_COLOR = 0xFF8B8B8B;
	
	private final UIEntity lowerEntity = new UIEntity();
	private final SignalBoxTileEntity box;
	private final ContainerSignalBox container;
	private UISignalBoxTile lastTile = null;
	
	public GuiSignalBox(SignalBoxTileEntity box) {
		this.box = box;
		this.container = new ContainerSignalBox(this::update);
		Minecraft.getMinecraft().player.openContainer = this.container;
		init();
		if (this.box.getTag() != null)
			this.compound = this.box.getTag();
		this.entity.read(this.compound);
	}
	
	private void update(NBTTagCompound compound) {
		this.resetSelection();
		if (compound.hasKey(SignalBoxTileEntity.ERROR_STRING)) {
			final String error = I18n.format(compound.getString(SignalBoxTileEntity.ERROR_STRING));
			final UIToolTip tooltip = new UIToolTip(error);
			entity.add(tooltip);
			new Thread(() -> {
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				entity.remove(tooltip);
			}).start();
			return;
		}
		this.entity.read(compound);
	}
	
	private void resetSelection() {
		this.entity.findRecursive(UIColor.class).stream().filter(color -> color.getColor() == SELECTION_COLOR).forEach(color -> color.getParent().remove(color));
	}
	
	private void modeInit(UIEntity parent, EnumGuiMode mode, Rotation rotation, SignalNode node, PathOption option) {
		final String modeName = I18n.format("property." + mode.name());
		final String rotationName = I18n.format("property." + rotation.name() + ".rotation");
		final UIEntity entity = new UIEntity();
		entity.setInheritWidth(true);
		entity.setHeight(20);
		entity.add(new UIColor(BACKGROUND_COLOR));
		entity.add(new UIScale(1.1f, 1.1f, 1));
		final UILabel modeLabel = new UILabel(modeName + " - " + rotationName);
		modeLabel.setCenterX(false);
		entity.add(modeLabel);
		parent.add(entity);
		
		if (mode.equals(EnumGuiMode.CORNER) || mode.equals(EnumGuiMode.STRAIGHT)) {
			final EnumPathUsage path = option.getPathUsage();
			final UIEntity stateEntity = new UIEntity();
			stateEntity.setInheritWidth(true);
			stateEntity.setHeight(15);
			final String pathUsageName = I18n.format("property.status") + ": ";
			final String pathUsage = I18n.format("property." + path);
			stateEntity.add(new UILabel(pathUsageName + pathUsage));
			parent.add(stateEntity);
			if (path.equals(EnumPathUsage.SELECTED) || path.equals(EnumPathUsage.USED)) {
				parent.add(GuiElements.createButton(I18n.format("button.reset"), e -> {
					option.setPathUsage(EnumPathUsage.FREE);
					node.write(compound);
				}));
			}
		}
		
		if (mode.equals(EnumGuiMode.STRAIGHT) || mode.equals(EnumGuiMode.CORNER)) {
			final SizeIntegerables<Integer> size = new SizeIntegerables<>("speed", 15, i -> i);
			final UIEntity speedSelection = GuiElements.createEnumElement(size, id -> {
				option.setSpeed(id > 0 ? id : Integer.MAX_VALUE);
				node.write(compound);
			});
			speedSelection.findRecursive(UIEnumerable.class).forEach(e -> {
				e.setID(null);
				e.setIndex(option.getSpeed() < 16 ? option.getSpeed() : Integer.MAX_VALUE);
			});
			parent.add(speedSelection);
		}
		
		if (mode.ordinal() >= EnumGuiMode.HP.ordinal() || mode.equals(EnumGuiMode.CORNER)) {
			final ImmutableList<BlockPos> positions = box.getPositions();
			if (!positions.isEmpty()) {
				final DisableIntegerable<BlockPos> blockPos = new DisableIntegerable<BlockPos>(SizeIntegerables.of("signal", positions.size(), positions::get));
				final UIEntity blockSelect = GuiElements.createEnumElement(blockPos, id -> {
					option.setLinkedPosition(id >= 0 ? positions.get(id) : null);
					node.write(compound);
				});
				blockSelect.findRecursive(UIEnumerable.class).forEach(e -> {
					e.setMin(-1);
					e.setIndex(positions.indexOf(option.getLinkedPosition()));
					e.setID(null);
				});
				parent.add(blockSelect);
			}
		}
		
		if (mode.equals(EnumGuiMode.HP)) {
			parent.add(GuiElements.createButton(I18n.format("button.reset"), e -> {
				this.lowerEntity.clear();
				GuiSyncNetwork.sendToPosServer(compound, this.box.getPos());
				mainPage(this::normalTile);
				final NBTTagCompound compound = new NBTTagCompound();
				final NBTTagCompound wayComp = new NBTTagCompound();
				toNBT(wayComp, POINT1, node.getPoint());
				compound.setTag(RESET_WAY, wayComp);
				GuiSyncNetwork.sendToPosServer(compound, this.box.getPos());
			}));
		}
		
	}
	
	public void initTileConfig(final SignalNode node) {
		if (node.isEmpty())
			return;
		reset();
		final UIEntity list = new UIEntity();
		list.setInheritHeight(true);
		list.setInheritWidth(true);
		final UIBox box = new UIBox(UIBox.VBOX, 1);
		list.add(box);
		lowerEntity.add(new UIBox(UIBox.VBOX, 3));
		lowerEntity.add(list);
		node.forEach((e, opt) -> modeInit(list, e.getKey(), e.getValue(), node, opt));
		lowerEntity.add(GuiElements.createPageSelect(box));
		lowerEntity.add(new UIClickable(e -> {
			reset();
			mainPage(this::normalTile);
		}, 1));
	}
	
	private void updateButton(UIEntity button) {
		final SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		button.findRecursive(UIClickable.class).forEach(c -> {
			final Consumer<UIEntity> old = c.getCallback();
			c.setCallback(e -> {
				reset();
				mainPage(this::normalTile);
				c.setCallback(old);
				handler.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			});
		});
	}
	
	private void settingsPage(UIEntity entity) {
		reset();
		lowerEntity.add(new UIBox(UIBox.VBOX, 2));
		box.forEach(p -> lowerEntity.add(GuiElements.createButton(p.toString(), e -> {
		})));
		lowerEntity.update();
		updateButton(entity);
	}
	
	private void editTile(UIEntity tile, UIMenu menu, final UISignalBoxTile sbt) {
		tile.add(new UIClickable(e -> {
			final SignalNode node = sbt.getNode();
			final EnumGuiMode mode = EnumGuiMode.values()[menu.getSelection()];
			final Rotation rotation = Rotation.values()[menu.getRotation()];
			if (node.has(mode, rotation)) {
				node.remove(mode, rotation);
			} else {
				node.add(mode, rotation);
			}
		}));
	}
	
	private void editPage(UIEntity entity) {
		reset();
		final UIMenu menu = new UIMenu();
		mainPage((e, name) -> this.editTile(e, menu, name));
		updateButton(entity);
		lowerEntity.add(menu);
	}
	
	private void normalTile(UIEntity tile, final UISignalBoxTile currentTile) {
		tile.add(new UIClickable(c -> {
			final SignalNode currentNode = currentTile.getNode();
			if (!(currentNode.has(EnumGuiMode.RS) || currentNode.has(EnumGuiMode.HP) || currentNode.has(EnumGuiMode.RA10)))
				return;
			c.add(new UIColor(SELECTION_COLOR));
			if (lastTile == null) {
				lastTile = currentTile;
			} else {
				if (lastTile == currentTile) {
					lastTile = null;
					this.resetSelection();
					return;
				}
				final NBTTagCompound comp = new NBTTagCompound();
				final NBTTagCompound way = new NBTTagCompound();
				toNBT(way, POINT1, lastTile.getNode().getPoint());
				toNBT(way, POINT2, currentNode.getPoint());
				comp.setTag(SignalBoxUtil.REQUEST_WAY, way);
				GuiSyncNetwork.sendToPosServer(comp, box.getPos());
				lastTile = null;
			}
		}));
		tile.add(new UIClickable(e -> initTileConfig(currentTile.getNode()), 1));
	}
	
	private void mainPage(BiConsumer<UIEntity, UISignalBoxTile> consumer) {
		lowerEntity.add(new UIColor(BACKGROUND_COLOR));
		lowerEntity.add(new UIStack());
		lowerEntity.add(new UIScissor());
		
		final UIEntity plane = new UIEntity();
		plane.setInheritWidth(true);
		plane.setInheritHeight(true);
		lowerEntity.add(new UIScroll(s -> {
			final float newScale = plane.getScaleX() + s * 0.001f;
			if (newScale <= 0)
				return;
			plane.setScaleX(newScale);
			plane.setScaleY(newScale);
			plane.update();
		}));
		lowerEntity.add(new UIDrag((x, y) -> {
			plane.setX(plane.getX() + x);
			plane.setY(plane.getY() + y);
			plane.update();
		}, 2));
		final UIBox vbox = new UIBox(UIBox.VBOX, 0);
		vbox.setPageable(false);
		plane.add(vbox);
		for (int x = 0; x < 50; x++) {
			final UIEntity row = new UIEntity();
			final UIBox hbox = new UIBox(UIBox.HBOX, 0);
			hbox.setPageable(false);
			row.add(hbox);
			row.setHeight(10);
			row.setWidth(10);
			for (int y = 0; y < 50; y++) {
				final UIEntity tile = new UIEntity();
				tile.setHeight(10);
				tile.setWidth(10);
				tile.add(new UIBorder(0xFF7F7F7F, 2));
				final Point name = new Point(y, x);
				final SignalNode node = new SignalNode(name);
				final UISignalBoxTile sbt = new UISignalBoxTile(node);
				tile.add(sbt);
				consumer.accept(tile, sbt);
				row.add(tile);
			}
			plane.add(row);
		}
		lowerEntity.add(plane);
		
		final UIEntity frame = new UIEntity();
		frame.setInheritHeight(true);
		frame.setInheritWidth(true);
		frame.add(new UIBorder(0xFF000000, 6));
		lowerEntity.add(frame);
		this.entity.read(compound);
	}
	
	private void init() {
		final String name = I18n.format("tile.signalbox.name");
		
		final UILabel titlelabel = new UILabel(name);
		titlelabel.setCenterX(false);
		
		final UIEntity titel = new UIEntity();
		titel.add(new UIScale(1.2f, 1.2f, 1));
		titel.add(titlelabel);
		titel.setInheritHeight(true);
		titel.setInheritWidth(true);
		
		final UIEntity header = new UIEntity();
		header.setInheritWidth(true);
		header.setHeight(20);
		header.add(new UIBox(UIBox.HBOX, 4));
		header.add(titel);
		header.add(GuiElements.createSpacerH(80));
		header.add(GuiElements.createButton(I18n.format("btn.settings"), this::settingsPage));
		header.add(GuiElements.createButton(I18n.format("btn.edit"), this::editPage));
		
		final UIEntity middlePart = new UIEntity();
		middlePart.setInheritHeight(true);
		middlePart.setInheritWidth(true);
		middlePart.add(new UIBox(UIBox.VBOX, 4));
		middlePart.add(header);
		middlePart.add(lowerEntity);
		
		lowerEntity.setInheritHeight(true);
		lowerEntity.setInheritWidth(true);
		mainPage(this::normalTile);
		
		this.entity.add(GuiElements.createSpacerH(10));
		this.entity.add(middlePart);
		this.entity.add(GuiElements.createSpacerH(10));
		this.entity.add(new UIBox(UIBox.HBOX, 1));
		
		this.entity.read(compound);
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		GuiSyncNetwork.sendToPosServer(compound, this.box.getPos());
	}
	
	private void reset() {
		this.entity.write(compound);
		GuiSyncNetwork.sendToPosServer(compound, this.box.getPos());
		lowerEntity.clear();
	}
}