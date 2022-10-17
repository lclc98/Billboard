package com.lclc98.billboard.network;

import com.lclc98.billboard.client.VideoHandler;
import com.lclc98.billboard.client.video.VideoDisplay;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent;

import java.util.function.Supplier;

public class SyncMessage {
    public BlockPos pos;
    public boolean paused;
    public long time;

    public SyncMessage() {

    }

    public SyncMessage(BlockPos pos, boolean paused, long time) {
        this.pos = pos;
        this.paused = paused;
        this.time = time;
    }

    public static void encode(SyncMessage message, PacketBuffer buf) {
        buf.writeBlockPos(message.pos);
        buf.writeBoolean(message.paused);
        buf.writeLong(message.time);
    }

    public static SyncMessage decode(PacketBuffer buf) {
        return new SyncMessage(buf.readBlockPos(), buf.readBoolean(), buf.readLong());
    }

    public static void handle(SyncMessage message, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayerEntity sender = ctx.get().getSender();
        if (sender != null) {
            VideoDisplay videoDisplay = VideoHandler.getVideo(message.pos);
            CallbackMediaPlayerComponent player = videoDisplay.player;
            player.mediaPlayer().submit(() -> player.mediaPlayer().controls().setPause(message.paused));
        }
    }
}
