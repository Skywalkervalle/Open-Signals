package com.troblecodings.signals.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.troblecodings.core.NBTWrapper;
import com.troblecodings.linkableapi.Linkingtool;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.items.ItemArmorTemplate;
import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.items.ToolParser;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class OSItems {

    private OSItems() {
    }

    public static final Linkingtool LINKING_TOOL = new Linkingtool(OSTabs.TAB, (world, pos) -> {
        final BlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        final boolean isRedstoneBlock = block == OSBlocks.REDSTONE_IN
                || block == OSBlocks.REDSTONE_OUT || block == OSBlocks.COMBI_REDSTONE_INPUT;
        return isRedstoneBlock || (block instanceof Signal && ((Signal) block).canBeLinked());
    }, _u -> true, (level, pos, tag) -> {
        final BlockState state = level.getBlockState(pos);
        new NBTWrapper(tag).putString(SignalControllerTileEntity.SIGNAL_NAME,
                state.getBlock().getRegistryName().getPath());
    });
    public static final Item CONDUCTOR_TROWEL_GREEN = new Item(
            new Properties().tab(CreativeModeTab.TAB_COMBAT));
    public static final Item CONDUCTOR_TROWEL_RED = new Item(
            new Properties().tab(CreativeModeTab.TAB_COMBAT));
    public static final Item WARNING_FLAG = new Item(
            new Properties().tab(CreativeModeTab.TAB_COMBAT));
    public static final Item K_BOARD = new Item(new Properties().tab(CreativeModeTab.TAB_COMBAT));
    public static final Item L_BOARD = new Item(new Properties().tab(CreativeModeTab.TAB_COMBAT));
    public static final ItemArmorTemplate REFLECTIVE_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.REFLECTIVE_ARMOR_MATERIAL, EquipmentSlot.HEAD);
    public static final ItemArmorTemplate REFLECTIVE_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.REFLECTIVE_ARMOR_MATERIAL, EquipmentSlot.CHEST);
    public static final ItemArmorTemplate REFLECTIVE_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.REFLECTIVE_ARMOR_MATERIAL, EquipmentSlot.LEGS);
    public static final ItemArmorTemplate REFLECTIVE_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.REFLECTIVE_ARMOR_MATERIAL, EquipmentSlot.FEET);
    public static final ItemArmorTemplate DISPATCHER_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.DISPATCHER_ARMOR_MATERIAL, EquipmentSlot.HEAD);
    public static final ItemArmorTemplate DISPATCHER_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.DISPATCHER_ARMOR_MATERIAL, EquipmentSlot.CHEST);
    public static final ItemArmorTemplate DISPATCHER_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.DISPATCHER_ARMOR_MATERIAL, EquipmentSlot.LEGS);
    public static final ItemArmorTemplate DISPATCHER_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.DISPATCHER_ARMOR_MATERIAL, EquipmentSlot.FEET);
    public static final ItemArmorTemplate STATION_MANAGER_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.STATIONMANAGER_ARMOR_MATERIAL, EquipmentSlot.HEAD);
    public static final ItemArmorTemplate STATION_MANAGER_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.STATIONMANAGER_ARMOR_MATERIAL, EquipmentSlot.CHEST);
    public static final ItemArmorTemplate STATION_MANAGER_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.STATIONMANAGER_ARMOR_MATERIAL, EquipmentSlot.LEGS);
    public static final ItemArmorTemplate STATION_MANAGER_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.STATIONMANAGER_ARMOR_MATERIAL, EquipmentSlot.FEET);
    public static final ItemArmorTemplate TRAIN_DRIVER_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.TRAINDRIVER_ARMOR_MATERIAL, EquipmentSlot.HEAD);
    public static final ItemArmorTemplate TRAIN_DRIVER_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.TRAINDRIVER_ARMOR_MATERIAL, EquipmentSlot.CHEST);
    public static final ItemArmorTemplate TRAIN_DRIVER_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.TRAINDRIVER_ARMOR_MATERIAL, EquipmentSlot.LEGS);
    public static final ItemArmorTemplate TRAIN_DRIVER_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.TRAINDRIVER_ARMOR_MATERIAL, EquipmentSlot.FEET);
    public static final ItemArmorTemplate CONDUCTOR_HEAD = new ItemArmorTemplate(
            ItemArmorTemplate.CONDUCTOR_ARMOR_MATERIAL, EquipmentSlot.HEAD);
    public static final ItemArmorTemplate CONDUCTOR_CHESTPLATE = new ItemArmorTemplate(
            ItemArmorTemplate.CONDUCTOR_ARMOR_MATERIAL, EquipmentSlot.CHEST);
    public static final ItemArmorTemplate CONDUCTOR_PANTS = new ItemArmorTemplate(
            ItemArmorTemplate.CONDUCTOR_ARMOR_MATERIAL, EquipmentSlot.LEGS);
    public static final ItemArmorTemplate CONDUCTOR_SHOES = new ItemArmorTemplate(
            ItemArmorTemplate.CONDUCTOR_ARMOR_MATERIAL, EquipmentSlot.FEET);
    public static final Item SIGNAL_PLATE = new Item(
            new Properties().tab(CreativeModeTab.TAB_MATERIALS));
    public static final Item SIGNAL_SHIELD = new Item(
            new Properties().tab(CreativeModeTab.TAB_MATERIALS));
    public static final Item LAMPS = new Item(new Properties().tab(CreativeModeTab.TAB_MATERIALS));
    public static final Item ELECTRIC_PARTS = new Item(
            new Properties().tab(CreativeModeTab.TAB_MATERIALS));

    public static ArrayList<Item> registeredItems = new ArrayList<>();

    public static ArrayList<Placementtool> placementtools = new ArrayList<>();

    public static void init() {
        synchronized (LINKING_TOOL) {
            if (LINKING_TOOL.getRegistryName() != null)
                return;
            final Field[] fields = OSItems.class.getFields();
            for (final Field field : fields) {
                final int modifiers = field.getModifiers();
                if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)
                        && Modifier.isPublic(modifiers)) {
                    final String name = field.getName().toLowerCase().replace("_", "");
                    try {
                        final Item item = (Item) field.get(null);
                        item.setRegistryName(new ResourceLocation(OpenSignalsMain.MODID, name));
                        registeredItems.add(item);
                    } catch (final IllegalArgumentException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            loadTools();
        }
    }

    private static final Gson GSON = new Gson();

    private static void loadTools() {
        OpenSignalsMain.contentPacks.getFiles("tools").forEach(entry -> {
            final ToolParser tools = GSON.fromJson(entry.getValue(), ToolParser.class);
            tools.getPlacementTools().forEach(placementtool -> {
                final Placementtool tool = new Placementtool();
                final String name = placementtool.toLowerCase().replace("_", "").trim();
                tool.setRegistryName(new ResourceLocation(OpenSignalsMain.MODID, name));
                placementtools.add(tool);
                registeredItems.add(tool);
            });
        });
    }

    @SubscribeEvent
    public static void registerItem(final RegistryEvent.Register<Item> event) {
        OSItems.init();
        final IForgeRegistry<Item> registry = event.getRegistry();
        registeredItems.forEach(registry::register);
    }

}
