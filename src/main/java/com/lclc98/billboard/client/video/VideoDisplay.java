package com.lclc98.billboard.client.video;

import com.lclc98.billboard.block.BillboardTileEntity;
import com.lclc98.billboard.util.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent;
import uk.co.caprica.vlcj.player.component.InputEvents;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallbackAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashSet;

public class VideoDisplay {
    private static Minecraft mc = Minecraft.getInstance();

    private static HashSet<Runnable> toBeRun = new HashSet<>();
    private static final String VLC_DOWNLOAD_32 = "https://i.imgur.com/qDIb9iV.png";
    private static final String VLC_DOWNLOAD_64 = "https://i.imgur.com/3EKo7Jx.png";
    private static final int ACCEPTABLE_SYNC_TIME = 1000;
    private static boolean isVLCInstalled = true;

    public static VideoDisplay createVideoDisplay(BillboardTileEntity te, float volume, boolean loop) {
        try {
            if (isVLCInstalled)
                return new VideoDisplay(te, volume, loop);
        } catch (Exception | UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
        isVLCInstalled = false;
//        String failURL = System.getProperty("sun.arch.data.model").equals("32") ? VLC_DOWNLOAD_32 : VLC_DOWNLOAD_64;
//        TextureCache cache = TextureCache.get(failURL);
//        if (cache.ready())
//            return cache.createDisplay(failURL, volume, loop, true);
        return null;
    }

    public int width = 1;
    public int height = 1;
    public CallbackMediaPlayerComponent player;
    private boolean stream = false;
    private float lastSetVolume;

    private String url;
    private float volume;
    private boolean loop;
    public ResourceLocation resourceLocation;

    private static NativeImage getDefaultTexture() throws IOException {
        IResource texture = Minecraft.getInstance().getResourceManager().getResource(TextureUtil.TEXTURE_BILLBOARD);
        final BufferedImage skinTexture = ImageIO.read(texture.getInputStream());

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(skinTexture, "png", os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        return NativeImage.read(is);
    }

    private boolean first;

    public VideoDisplay(BillboardTileEntity te, float volume, boolean loop) {
        super();
        this.url = te.getTextureUrl();
        this.volume = volume;
        this.loop = loop;
        this.resourceLocation = te.getTextureLocation();
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();

        try {
            textureManager.register(this.resourceLocation, new DynamicTexture(getDefaultTexture()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        player = new CallbackMediaPlayerComponent(new MediaPlayerFactory(), null, InputEvents.NONE, false, new RenderCallback() {
            private void blah(ByteBuffer buffer) {
                Texture t = textureManager.getTexture(resourceLocation);
                if (t != null) {
                    if (first) {
                        RenderSystem.pushMatrix();
                        RenderSystem.enableTexture();
                        RenderSystem.bindTexture(t.getId());
                        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                        RenderSystem.disableTexture();
                        RenderSystem.popMatrix();
                        first = false;
                    } else {
                        RenderSystem.pushMatrix();
                        RenderSystem.enableTexture();
                        RenderSystem.bindTexture(t.getId());
                        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                        RenderSystem.disableTexture();
                        RenderSystem.popMatrix();
                    }
                }
            }

            @Override
            public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
                if (!RenderSystem.isOnRenderThread()) {
                    RenderSystem.recordRenderCall(() -> {
                        blah(nativeBuffers[0]);
                    });
                } else {
                    blah(nativeBuffers[0]);
                }

            }
        }, new BufferFormatCallbackAdapter() {

            @Override
            public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                synchronized (this) {
                    VideoDisplay.this.width = sourceWidth;
                    VideoDisplay.this.height = sourceHeight;
                    VideoDisplay.this.first = true;
                }
                return new BufferFormat("RGBA", sourceWidth, sourceHeight, new int[]{sourceWidth * 4}, new int[]{sourceHeight});
            }
        }, null);
        player.mediaPlayer().submit(() -> {
            player.mediaPlayer().audio().setVolume((int) (volume * 100F));
            lastSetVolume = volume;
            player.mediaPlayer().controls().setRepeat(loop);
            player.mediaPlayer().media().start(url);
        });
    }

    public void prepare(boolean playing, int tick) {
        if (player.mediaPlayer().media().isValid()) {
            boolean realPlaying = playing && !Minecraft.getInstance().isPaused();

            if (volume != lastSetVolume) {
                player.mediaPlayer().submit(() -> player.mediaPlayer().audio().setVolume((int) (volume * 100F)));
                lastSetVolume = volume;
            }
            if (player.mediaPlayer().controls().getRepeat() != loop)
                player.mediaPlayer().submit(() -> player.mediaPlayer().controls().setRepeat(loop));
            long tickTime = 50;
            long newDuration = player.mediaPlayer().status().length();
            if (!stream && newDuration != -1 && newDuration != 0 && player.mediaPlayer().media().info().duration() == 0)
                stream = true;
            if (stream) {
                if (player.mediaPlayer().status().isPlaying() != realPlaying)
                    player.mediaPlayer().submit(() -> player.mediaPlayer().controls().setPause(!realPlaying));
            } else {
                if (player.mediaPlayer().status().length() > 0) {
                    long time = tick * tickTime + (realPlaying ? (long) (getDeltaFrameTime() * tickTime) : 0);
                    if (player.mediaPlayer().status().isSeekable() && time > player.mediaPlayer().status().time())
                        if (loop)
                            time %= player.mediaPlayer().status().length();
                    if (Math.abs(time - player.mediaPlayer().status().time()) > ACCEPTABLE_SYNC_TIME)
                        player.mediaPlayer().submit(() -> {
                            long newTime = tick * tickTime + (realPlaying ? (long) (getDeltaFrameTime() * tickTime) : 0);
                            if (player.mediaPlayer().status().isSeekable() && newTime > player.mediaPlayer().status().length())
                                if (loop)
                                    newTime %= player.mediaPlayer().status().length();

                            player.mediaPlayer().controls().setTime(newTime);
                            if (player.mediaPlayer().status().isPlaying() != realPlaying)
                                player.mediaPlayer().controls().setPause(!realPlaying);
                        });
                }
            }
        }
    }

    public void release() {
        Runnable run = new Runnable() {

            @Override
            public void run() {
                player.release();
                synchronized (toBeRun) {
                    toBeRun.remove(this);
                }
            }
        };

        synchronized (toBeRun) {
            toBeRun.add(run);
        }
        player.mediaPlayer().submit(run);
    }

    public static float getDeltaFrameTime() {
        if (mc.isPaused())
            return 1.0F;
        return mc.getDeltaFrameTime();
    }
}
