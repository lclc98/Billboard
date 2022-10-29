package com.lclc98.billboard.network;

import com.lclc98.billboard.block.BillboardTileEntity;
import com.lclc98.billboard.util.TextureUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateMessage {
    public BlockPos pos;
    public String textureUrl;
    public boolean locked;
    public int rotation;

    public UpdateMessage() {

    }

    public UpdateMessage(BlockPos pos, String textureUrl, boolean locked, int rotation) {
        this.pos = pos;
        this.textureUrl = textureUrl;
        this.locked = locked;
        this.rotation = rotation;
    }

    public static void encode(UpdateMessage message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.pos);
        buf.writeUtf(message.textureUrl);
        buf.writeBoolean(message.locked);
        buf.writeInt(message.rotation);
    }

    public static UpdateMessage decode(FriendlyByteBuf buf) {
        return new UpdateMessage(buf.readBlockPos(), buf.readUtf(), buf.readBoolean(), buf.readInt());
    }

    public static void handle(UpdateMessage message, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer sender = ctx.get().getSender();
        if (sender != null) {
            BlockEntity te = sender.level.getBlockEntity(message.pos);
            if (te instanceof BillboardTileEntity) {
                BillboardTileEntity billboard = (BillboardTileEntity) te;

                if (billboard.hasPermission(sender)) {
                    billboard.rotation = message.rotation;
                    if (TextureUtil.validateUrl(message.textureUrl)) {
                        billboard.setTexture(message.textureUrl);
                    }
                    if (billboard.ownerId.equals(sender.getUUID())) {
                        billboard.locked = message.locked;
                    }
                    billboard.sync();
                }
            }
        }
    }
}
