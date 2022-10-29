package com.lclc98.billboard.block;

import com.lclc98.billboard.Billboard;
import com.mojang.math.Vector4f;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BillboardTileEntity extends BlockEntity {

    private final LazyLoadedValue<ChunkPos> chunkPos;

    public UUID ownerId;
    public boolean locked = true;
    private Set<BlockPos> children = new HashSet<>();
    private BlockPos parentPos;
    public int rotation = 0;

    public int minWidth;
    public int minHeight;
    public int maxWidth;
    public int maxHeight;
    public Vector4f uv;
    public boolean dirty;

    private String textureUrl = "https://i.imgur.com/y9S27IN.png";
    private ResourceLocation textureLocation = new ResourceLocation(Billboard.MOD_ID, "billboards/" + DigestUtils.sha256Hex(this.textureUrl));

    public BillboardTileEntity(BlockPos blockPos, BlockState blockState) {
        super(Billboard.BILLBOARD_TE_TYPE.get(), blockPos, blockState);
        this.chunkPos = new LazyLoadedValue<>(() -> new ChunkPos(this.worldPosition));
    }

    public void setTexture(String textureUrl) {
        this.textureUrl = textureUrl;
        this.textureLocation = new ResourceLocation(Billboard.MOD_ID, "billboards/" + DigestUtils.sha256Hex(this.textureUrl));
    }

    public ResourceLocation getTextureLocation() {
        return this.textureLocation;
    }

    public String getTextureUrl() {
        return this.textureUrl;
    }

    public void setUV(Vector4f uv) {
        this.uv = uv;
    }

    public Vector4f getUV() {
        if (this.uv == null) {
            return new Vector4f(0, 0, 1, 1);
        }
        return this.uv;
    }

    public void addChild(BlockPos childPos) {
        BillboardTileEntity parent = this.getParent();

        if (parent == null) {
            return;
        }

        if (!parent.children.contains(childPos)) {
            parent.children.add(childPos);

            BlockEntity childTe = parent.level.getBlockEntity(childPos);
            if (childTe instanceof BillboardTileEntity childBillboard) {
                childBillboard.parentPos = parent.worldPosition;
            }
        }
        parent.sync();
    }

    public boolean isParent() {
        return this.parentPos == null;
    }

    public Set<BlockPos> getChildren() {
        return this.children;
    }

    public BillboardTileEntity getParent() {
        if (isParent()) {
            return this;
        }

        BlockEntity tileEntity = this.level.getBlockEntity(this.parentPos);
        if (tileEntity instanceof BillboardTileEntity) {
            return (BillboardTileEntity) tileEntity;
        }

        this.parentPos = null;
        return null;
    }

    public void sync() {
        if (this.level instanceof ServerLevel) {
            if (this.isParent()) {
                for (BlockPos pos : this.children) {
                    BlockEntity childTE = this.level.getBlockEntity(pos);
                    if (childTE instanceof BillboardTileEntity) {
                        ((BillboardTileEntity) childTE).dirty = true;
                        ((BillboardTileEntity) childTE).sync();
                    }
                }
            }
            this.dirty = true;
            final Packet<ClientGamePacketListener> packet = this.getUpdatePacket();
            sendToTracking((ServerLevel) this.level, this.chunkPos.get(), packet, false);
        }
    }

    public static void sendToTracking(ServerLevel world, ChunkPos chunkPos, Packet<ClientGamePacketListener> packet, boolean boundaryOnly) {
        world.getChunkSource().chunkMap.getPlayers(chunkPos, boundaryOnly).forEach(p -> p.connection.send(packet));
    }

    public void blockBroken() {
        super.setRemoved();

        if (this.level != null && this.level.isClientSide) {
            return;
        }

        if (this.isParent()) {
            if (!this.children.isEmpty()) {
                BlockPos newParent = this.children.stream().findFirst().get();
                this.children.remove(newParent);
                BlockEntity blockEntity = this.level.getBlockEntity(newParent);
                if (blockEntity instanceof BillboardTileEntity billboard) {
                    billboard.parentPos = null;
                    billboard.children = this.children;
                    billboard.ownerId = this.ownerId;
                    billboard.locked = this.locked;
                    billboard.rotation = this.rotation;
                    billboard.setTexture(this.textureUrl);
                    for (BlockPos pos : billboard.children) {
                        BlockEntity childTE = this.level.getBlockEntity(pos);
                        if (childTE instanceof BillboardTileEntity childBillboard) {
                            childBillboard.parentPos = newParent;
                        }
                    }
                    billboard.sync();
                }
            }
        } else {
            BlockEntity tileEntity = this.level.getBlockEntity(this.parentPos);
            if (tileEntity instanceof BillboardTileEntity billboard) {
                billboard.children.remove(this.worldPosition);
                billboard.dirty = true;
                billboard.sync();
            }
        }
    }

    public boolean hasPermission(Player player) {
        if (this.ownerId == null) {
            this.ownerId = player.getUUID();
        }
        return !this.locked || player.hasPermissions(2) || this.ownerId.equals(player.getUUID());
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        if (nbt.contains("ownerId")) {
            this.ownerId = nbt.getUUID("ownerId");
        }

        if (nbt.contains("locked")) {
            this.locked = nbt.getBoolean("locked");
        }

        if (nbt.contains("textureUrl")) {
            this.setTexture(nbt.getString("textureUrl"));
        }

        if (nbt.contains("parentX")) {
            int x = nbt.getInt("parentX");
            int y = nbt.getInt("parentY");
            int z = nbt.getInt("parentZ");
            this.parentPos = new BlockPos(x, y, z);
        }

        this.rotation = nbt.getInt("rotation");

        this.children.clear();
        if (nbt.contains("children")) {
            ListTag childrenTags = nbt.getList("children", 10);
            for (Tag tag : childrenTags) {
                CompoundTag child = (CompoundTag) tag;
                int x = child.getInt("posX");
                int y = child.getInt("posY");
                int z = child.getInt("posZ");
                this.children.add(new BlockPos(x, y, z));
            }
        }
        this.dirty = true;
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        if (this.ownerId != null) {
            compound.putUUID("ownerId", this.ownerId);
        }

        compound.putBoolean("locked", this.locked);

        if (this.textureUrl != null) {
            compound.putString("textureUrl", this.textureUrl);
        }

        if (this.parentPos != null) {
            compound.putInt("parentX", this.parentPos.getX());
            compound.putInt("parentY", this.parentPos.getY());
            compound.putInt("parentZ", this.parentPos.getZ());
        }

        compound.putInt("rotation", this.rotation);

        if (!this.children.isEmpty()) {
            ListTag tagList = new ListTag();
            for (BlockPos child : this.children) {
                CompoundTag childCompound = new CompoundTag();
                childCompound.putInt("posX", child.getX());
                childCompound.putInt("posY", child.getY());
                childCompound.putInt("posZ", child.getZ());
                tagList.add(childCompound);
            }
            compound.put("children", tagList);
        }
    }

    @Override
    public void requestModelDataUpdate() {
        super.requestModelDataUpdate();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag updateTag = super.getUpdateTag();
        saveAdditional(updateTag);
        return updateTag;
    }
}
