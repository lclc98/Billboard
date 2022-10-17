package com.lclc98.billboard.util;

import com.lclc98.billboard.Billboard;
import com.lclc98.billboard.block.BillboardTileEntity;
import com.lclc98.billboard.client.video.VideoDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class TextureUtil {

    public static final ResourceLocation TEXTURE_BILLBOARD = new ResourceLocation(Billboard.MOD_ID, "textures/billboard/billboard.png");

    public static boolean validateUrl(String url) {
        return true;
//        return Billboard.config.getPattern().matcher(url).find();
    }

    //https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4
    public static ResourceLocation getTexture(BillboardTileEntity te) {

        ResourceLocation textureLocation = te.getTextureLocation();
        TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
        Texture texture = texturemanager.getTexture(textureLocation);
        if (texture == null) {
            try {
                texture = new DynamicTexture(getNativeImage(te.getTextureUrl()));
                texturemanager.register(textureLocation, texture);
            } catch (IOException e) {
                e.printStackTrace();
                return TEXTURE_BILLBOARD;
            }
        }

        return textureLocation;
    }

    private static NativeImage getNativeImage(String url) throws IOException {
        InputStream is = new ByteArrayInputStream(IOUtils.toByteArray(new URL(url)));
        return NativeImage.read(is);
    }
}
