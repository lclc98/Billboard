package com.lclc98.billboard.block;

import com.lclc98.billboard.Billboard;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.LazyValue;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.server.ServerWorld;

import java.util.HashSet;
import java.util.Set;

public class BillboardTileEntity extends TileEntity {

    private final LazyValue<ChunkPos> chunkPos;

    private String textureId = "DHHCsdx";
    private ResourceLocation textureLocation = new ResourceLocation(Billboard.MOD_ID, "billboards/" + textureId.toLowerCase());

    private Set<BlockPos> children = new HashSet<>();
    private BlockPos parentPos;

    public int minWidth;
    public int minHeight;
    public int maxWidth;
    public int maxHeight;
    public Vector4f uv;
    public boolean dirty;

    public BillboardTileEntity() {
        super(Billboard.BILLBOARD_TE_TYPE);
        this.chunkPos = new LazyValue<>(() -> new ChunkPos(this.pos));
    }

    public void setTexture(String textureId) {
        this.textureId = textureId;
        this.textureLocation = new ResourceLocation(Billboard.MOD_ID, "billboards/" + textureId.toLowerCase());
    }

    public static void sendToTracking(ServerWorld world, ChunkPos chunkPos, IPacket<?> packet, boolean boundaryOnly) {

        world.getChunkProvider().chunkManager.getTrackingPlayers(chunkPos, boundaryOnly).forEach(p -> p.connection.sendPacket(packet));
    }

    public ResourceLocation getTextureLocation() {
        return this.textureLocation;
    }

    public String getTextureId() {
        return this.textureId;
    }

    public void addChild(BlockPos childPos) {
        BillboardTileEntity parent = this.getParent();

        if (parent == null) {
            return;
        }

        if (!parent.children.contains(childPos)) {
            parent.children.add(childPos);

            TileEntity childTe = parent.world.getTileEntity(childPos);
            if (childTe instanceof BillboardTileEntity) {
                BillboardTileEntity childBillboard = ((BillboardTileEntity) childTe);
                childBillboard.parentPos = parent.pos;
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
        TileEntity tileEntity = this.world.getTileEntity(this.parentPos);
        if (tileEntity instanceof BillboardTileEntity) {
            return (BillboardTileEntity) tileEntity;
        }

        this.parentPos = null;
        return null;
    }

    public void sync() {
        if (this.world instanceof ServerWorld) {
            if(this.isParent()){
                for (BlockPos pos : this.children) {
                    TileEntity childTE = this.world.getTileEntity(pos);
                    if (childTE instanceof BillboardTileEntity) {
                        ((BillboardTileEntity) childTE).sync();
                    }
                }
            }
            final IPacket<?> packet = this.getUpdatePacket();
            sendToTracking((ServerWorld) this.world, this.chunkPos.getValue(), packet, false);
        } else {
            System.out.println("ERROR");
        }
    }

    @Override
    public void remove() {
        super.remove();

        if (this.world != null && this.world.isRemote) {
            return;
        }

        if (this.isParent()) {
            if (!this.children.isEmpty()) {
                BlockPos newParent = this.children.stream().findFirst().get();
                this.children.remove(newParent);
                TileEntity tileEntity = this.world.getTileEntity(newParent);
                if (tileEntity instanceof BillboardTileEntity) {
                    BillboardTileEntity billboard = (BillboardTileEntity) tileEntity;
                    billboard.parentPos = null;
                    billboard.children = this.children;
                    billboard.setTexture(this.textureId);
                    for (BlockPos pos : billboard.children) {
                        TileEntity childTE = this.world.getTileEntity(pos);
                        if (childTE instanceof BillboardTileEntity) {
                            BillboardTileEntity childBillboard = ((BillboardTileEntity) childTE);
                            childBillboard.parentPos = newParent;
                        }
                    }
                    billboard.sync();
                }
            }
        } else {
            TileEntity tileEntity = this.world.getTileEntity(this.parentPos);
            if (tileEntity instanceof BillboardTileEntity) {
                BillboardTileEntity billboard = (BillboardTileEntity) tileEntity;
                billboard.children.remove(this.pos);
                billboard.dirty = true;
                billboard.sync();
            }
        }
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);

        if (nbt.contains("textureId")) {
            this.setTexture(nbt.getString("textureId"));
        }

        if (nbt.contains("parentX")) {
            int x = nbt.getInt("parentX");
            int y = nbt.getInt("parentY");
            int z = nbt.getInt("parentZ");
            this.parentPos = new BlockPos(x, y, z);
        }

        this.children.clear();
        if (nbt.contains("children")) {
            ListNBT childrenTags = nbt.getList("children", 10);
            for (INBT tag : childrenTags) {
                CompoundNBT child = (CompoundNBT) tag;
                int x = child.getInt("posX");
                int y = child.getInt("posY");
                int z = child.getInt("posZ");
                this.children.add(new BlockPos(x, y, z));
            }
        }
        this.dirty = true;
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        if (this.textureId != null) {
            compound.putString("textureId", this.textureId);
        }

        if (this.parentPos != null) {
            compound.putInt("parentX", this.parentPos.getX());
            compound.putInt("parentY", this.parentPos.getY());
            compound.putInt("parentZ", this.parentPos.getZ());
        }

        if (!this.children.isEmpty()) {
            ListNBT tagList = new ListNBT();
            for (BlockPos child : this.children) {
                CompoundNBT childCompound = new CompoundNBT();
                childCompound.putInt("posX", child.getX());
                childCompound.putInt("posY", child.getY());
                childCompound.putInt("posZ", child.getZ());
                tagList.add(childCompound);
            }
            compound.put("children", tagList);
        }

        return super.write(compound);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        super.onDataPacket(net, packet);
        this.read(this.getBlockState(), packet.getNbtCompound());
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
}
