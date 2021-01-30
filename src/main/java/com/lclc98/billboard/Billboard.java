package com.lclc98.billboard;

import com.lclc98.billboard.block.BillboardBlock;
import com.lclc98.billboard.block.BillboardTileEntity;
import com.lclc98.billboard.client.renderer.tileentity.BillBoardTileEntityRender;
import com.lclc98.billboard.network.UpdateMessage;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid = Billboard.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(value = Billboard.MOD_ID)
public class Billboard {
    public final static String MOD_ID = "billboard";

    public static final Block BILLBOARD_BLOCK = new BillboardBlock();
    public static final TileEntityType<BillboardTileEntity> BILLBOARD_TE_TYPE = TileEntityType.Builder.create(BillboardTileEntity::new, BILLBOARD_BLOCK).build(null);
    public static Configuration config;

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public Billboard() {
        NETWORK.registerMessage(0, UpdateMessage.class, UpdateMessage::encode, UpdateMessage::decode, (message, context) -> context.get().enqueueWork(() -> {
            UpdateMessage.handle(message, context);
            context.get().setPacketHandled(true);
        }));

        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setupClient);
        config = new Configuration();
    }

    private void setupClient(FMLClientSetupEvent event) {
        ClientRegistry.bindTileEntityRenderer(BILLBOARD_TE_TYPE, BillBoardTileEntityRender::new);
    }

    @SubscribeEvent
    public static void registerBlock(RegistryEvent.Register<Block> register) {
        register.getRegistry().register(BILLBOARD_BLOCK);
    }

    @SubscribeEvent
    public static void registerItem(RegistryEvent.Register<Item> register) {
        Item item = new BlockItem(BILLBOARD_BLOCK, new Item.Properties().group(ItemGroup.MISC));
        item.setRegistryName(BILLBOARD_BLOCK.getRegistryName());
        register.getRegistry().register(item);
    }

    @SubscribeEvent
    public static void registerTileEntity(RegistryEvent.Register<TileEntityType<?>> register) {
        BILLBOARD_TE_TYPE.setRegistryName(new ResourceLocation(MOD_ID, "billboard"));
        register.getRegistry().register(BILLBOARD_TE_TYPE);
    }
}
