package com.lclc98.billboard.network;

import com.lclc98.billboard.block.BillboardTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class UpdateMessage {
    public BlockPos pos;
    public String textureId;
    public boolean locked;

    public UpdateMessage() {

    }

    public UpdateMessage(BlockPos pos, String textureId, boolean locked) {
        this.pos = pos;
        this.textureId = textureId;
        this.locked = locked;
    }

    public static void encode(UpdateMessage message, PacketBuffer buf) {
        buf.writeBlockPos(message.pos);
        buf.writeString(message.textureId);
        buf.writeBoolean(message.locked);
    }

    public static UpdateMessage decode(PacketBuffer buf) {
        return new UpdateMessage(buf.readBlockPos(), buf.readString(32767), buf.readBoolean());
    }

    public static void handle(UpdateMessage message, Supplier<NetworkEvent.Context> ctx) {
        ServerPlayerEntity sender = ctx.get().getSender();
        if (sender != null) {
            TileEntity te = sender.world.getTileEntity(message.pos);
            if (te instanceof BillboardTileEntity) {
                BillboardTileEntity billboard = (BillboardTileEntity) te;

                UUID uuid = sender.getUniqueID();
                if (billboard.ownerId == null) {
                    billboard.ownerId = uuid;
                }

                if (!billboard.locked || sender.hasPermissionLevel(2) || billboard.ownerId.equals(uuid)) {
                    billboard.setTexture(message.textureId);
                    if (billboard.ownerId.equals(uuid)) {
                        billboard.locked = message.locked;
                    }
                    billboard.sync();
                }
            }
        }
    }
}
