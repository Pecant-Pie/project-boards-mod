package com.pecantpie;

import com.mojang.serialization.MapCodec;
import com.pecantpie.block.TaskBoardBlock;
import com.pecantpie.block.TaskBoardBlockEntity;
import com.pecantpie.block.TaskBoardRenderer;
import com.pecantpie.component.ModDataComponents;
import com.pecantpie.item.TaskBoardItem;
import com.pecantpie.item.TaskSlipItem;
import com.pecantpie.screen.TaskBoardMenu;
import com.pecantpie.screen.TaskBoardScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ProjectBoards.MODID)
public class ProjectBoards
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "projectboards";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "projectboards" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    // Create a Deferred Register to hold BlockEntityTypes which will all be registered under the "projectboards" namespace
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ProjectBoards.MODID);

    // Create a Deferred Register to hold Items which will all be registered under the "projectboards" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // Create a register to hold ContainerMenus
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ProjectBoards.MODID);

    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "projectboards" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Task Slip item
    public static final DeferredItem<TaskSlipItem> TASK_SLIP = ITEMS.registerItem("task_slip", TaskSlipItem::new);

    // Creates a new Block with the id "projectboards:task_board", combining the namespace and path
    public static final DeferredBlock<TaskBoardBlock> TASK_BOARD = BLOCKS.registerBlock("task_board",
            (props) -> new TaskBoardBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(SoundType.WOOD).strength(1f)));
    // Creates a new BlockItem with the id "projectboards:task_board", combining the namespace and path
    public static final DeferredItem<TaskBoardItem> TASK_BOARD_ITEM = ITEMS.register("task_board", () -> new TaskBoardItem(TASK_BOARD.get(), new Item.Properties()));


    // Task Board Menu registration!!
    public static final DeferredHolder<MenuType<?>, MenuType<TaskBoardMenu>> TASK_BOARD_MENU = MENUS.register("task_board_menu", () -> IMenuTypeExtension.create(TaskBoardMenu::new));

    public static final DeferredRegister<MapCodec<? extends Block>> BLOCK_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, MODID);

    public static final Supplier<MapCodec<TaskBoardBlock>> SIMPLE_CODEC = BLOCK_TYPES.register(
            "simple",
            () -> BlockBehaviour.simpleCodec(TaskBoardBlock::new)
    );

    public static final Supplier<BlockEntityType<TaskBoardBlockEntity>> TASK_BOARD_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("task_board_block_entity", () -> BlockEntityType.Builder.of(TaskBoardBlockEntity::new, TASK_BOARD.get()).build(null));

    public static final BlockCapability<IItemHandler, Void> TASK_BOARD_ITEM_HANDLER =
            BlockCapability.createVoid(
                    // Provide a name to uniquely identify the capability.
                    ResourceLocation.fromNamespaceAndPath(MODID, "task_board_item_handler"),
                    // Provide the queried type. Here, we want to look up `IItemHandler` instances.
                    IItemHandler.class);

    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);



    // Creates a creative tab with the id "projectboards:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PROJECT_BOARDS_TAB = CREATIVE_MODE_TABS.register("project_boards", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.projectboards")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> TASK_BOARD_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(TASK_BOARD_ITEM.get());
                output.accept(TASK_SLIP.get());
            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ProjectBoards(IEventBus modEventBus, ModContainer modContainer)
    {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);

        // Register the Deferred Register to the mod event bus so block entities get registered
        BLOCK_ENTITY_TYPES.register(modEventBus);

        // Register the menus
        MENUS.register(modEventBus);

        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);

        ModDataComponents.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ProjectBoards) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));


        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(TASK_SLIP);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }



    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        @SubscribeEvent
        public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(TASK_BOARD_BLOCK_ENTITY.get(), TaskBoardRenderer::new);
        }

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(TASK_BOARD_MENU.get(), TaskBoardScreen::new);
        }
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModEvents {

        @SubscribeEvent
        public static void register(final RegisterPayloadHandlersEvent event) {
            final PayloadRegistrar registrar = event.registrar("1");
            registrar.playToServer(
                    ProjectBoardData.EditTaskData.TYPE,
                    ProjectBoardData.EditTaskData.STREAM_CODEC,
                    ProjectBoardData.EditTaskData.ServerPayloadHandler::handleDataOnMain
            );
        }

        @SubscribeEvent  // on the mod event bus
        public static void registerCapabilities(RegisterCapabilitiesEvent event) {
            event.registerBlockEntity(
                    Capabilities.ItemHandler.BLOCK, // capability to register for
                    TASK_BOARD_BLOCK_ENTITY.get(), // block entity type to register for
                    (myBlockEntity, side) -> myBlockEntity.getItemHandler()
            );
        }
    }
}
