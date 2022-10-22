package com.lclc98.billboard.client;

import com.lclc98.billboard.Billboard;
import com.lclc98.billboard.client.renderer.tileentity.BillBoardTileEntityRender;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Billboard.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientListener {

    @SubscribeEvent
    public static void onRe(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Billboard.BILLBOARD_TE_TYPE.get(), BillBoardTileEntityRender::new);
    }
}
