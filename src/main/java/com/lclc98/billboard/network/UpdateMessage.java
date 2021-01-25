package com.lclc98.billboard.network;

import com.lclc98.billboard.block.BillboardTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateMessage {
    public String textureId;
    public BlockPos pos;

    public UpdateMessage() {

    }

    public UpdateMessage(String textureId, BlockPos pos) {
        this.textureId = textureId;
        this.pos = pos;
    }

    public static void encode(UpdateMessage message, PacketBuffer buf) {
        buf.writeString(message.textureId);
        buf.writeBlockPos(message.pos);
    }

    public static UpdateMessage decode(PacketBuffer buf) {

        return new UpdateMessage(buf.readString(32767), buf.readBlockPos());
    }

    public static void handle(UpdateMessage message, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayerEntity sender = ctx.get().getSender();
        if (sender != null) {
            TileEntity te = sender.world.getTileEntity(message.pos);
            if (te instanceof BillboardTileEntity) {
                BillboardTileEntity billboard = (BillboardTileEntity) te;
                billboard.setTexture(message.textureId);
                billboard.sync();
            }
        }
    }
}
