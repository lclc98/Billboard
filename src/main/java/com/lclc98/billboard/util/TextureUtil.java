package com.lclc98.billboard.util;

import com.lclc98.billboard.Billboard;
import com.lclc98.billboard.block.BillboardTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class TextureUtil {

    private static final ResourceLocation TEXTURE_BILLBOARD = new ResourceLocation(Billboard.MOD_ID, "textures/billboard/billboard.png");

    public static boolean validateUrl(String url) {
        return Billboard.config.getPattern().matcher(url).find();
    }

    public static ResourceLocation getTexture(BillboardTileEntity te) {
        ResourceLocation textureLocation = te.getTextureLocation();
        TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
        AbstractTexture texture = texturemanager.getTexture(textureLocation, null);
        if (texture == null) {
            String fileName = FilenameUtils.getName(te.getTextureUrl());
            texture = new HttpTexture(new File("cache/billboard", fileName), te.getTextureUrl(), TEXTURE_BILLBOARD, false, null);
            texturemanager.register(textureLocation, texture);
        }

        return textureLocation;
    }
}
