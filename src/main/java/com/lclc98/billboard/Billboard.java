package com.lclc98.billboard;

import com.lclc98.billboard.block.BillboardBlock;
import com.lclc98.billboard.block.BillboardTileEntity;
import com.lclc98.billboard.network.UpdateMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Billboard.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(value = Billboard.MOD_ID)
public class Billboard {
    public final static String MOD_ID = "billboard";

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID);

    public static final RegistryObject<Block> BILLBOARD_BLOCK = BLOCKS.register("billboard", BillboardBlock::new);
    public static final RegistryObject<Item> BILLBOARD_BLOCK_ITEM = ITEMS.register("billboard", () -> new BlockItem(BILLBOARD_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_MISC)));

    public static final RegistryObject<BlockEntityType<BillboardTileEntity>> BILLBOARD_TE_TYPE = BLOCK_ENTITY_TYPES.register(
            "billboard", () -> BlockEntityType.Builder.of(BillboardTileEntity::new, BILLBOARD_BLOCK.get()).build(null)
    );

    public static Configuration config;

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public Billboard() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCK_ENTITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());

        NETWORK.registerMessage(0, UpdateMessage.class, UpdateMessage::encode, UpdateMessage::decode, (message, context) -> context.get().enqueueWork(() -> {
            UpdateMessage.handle(message, context);
            context.get().setPacketHandled(true);
        }));

        config = new Configuration();
    }


}
