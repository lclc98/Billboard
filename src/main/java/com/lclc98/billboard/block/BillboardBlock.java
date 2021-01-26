package com.lclc98.billboard.block;

import com.lclc98.billboard.Billboard;
import com.lclc98.billboard.util.RenderUtil;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.UUID;

public class BillboardBlock extends ContainerBlock {
    public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

    public BillboardBlock() {
        super(Properties.create(Material.WOOD).doesNotBlockMovement());
        this.setRegistryName(new ResourceLocation(Billboard.MOD_ID, "billboard"));
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof BillboardTileEntity) {
                BillboardTileEntity parent = ((BillboardTileEntity) te).getParent();
                if (!parent.locked || player.hasPermissionLevel(2) || parent.ownerId == null|| parent.ownerId.equals(player.getUniqueID()) ) {
                    RenderUtil.openGui(parent);
                } else {
                    player.sendStatusMessage(new StringTextComponent("You don't have permission to open this."), false);
                }
            }
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (worldIn.isRemote || placer == null) {
            return;
        }
        UUID uuid = placer.getUniqueID();

        if (!placer.isSneaking()) {
            Direction currentFacing = state.get(FACING);

            if (this.addParent(uuid, worldIn, pos, pos.offset(Direction.UP), currentFacing)) {
                return;
            }
            if (this.addParent(uuid, worldIn, pos, pos.offset(Direction.DOWN), currentFacing)) {
                return;
            }
            if (this.addParent(uuid, worldIn, pos, pos.offset(state.get(FACING).rotateY()), currentFacing)) {
                return;
            }
            if (this.addParent(uuid, worldIn, pos, pos.offset(state.get(FACING).rotateYCCW()), currentFacing)) {
                return;
            }
        }
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof BillboardTileEntity) {
            BillboardTileEntity parent = (BillboardTileEntity) te;
            parent.ownerId = uuid;
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if (!state.hasProperty(FACING)) {
            return VoxelShapes.fullCube();
        }
        final float f = 1.0f / 16.0f;
        Direction direction = state.get(FACING);

        float x1 = direction == Direction.WEST ? 1.0f - f : 0;
        float x2 = direction == Direction.EAST ? f : 1;
        float z1 = direction == Direction.NORTH ? 1.0f - f : 0;
        float z2 = direction == Direction.SOUTH ? f : 1;
        return VoxelShapes.create(x1, 0, z1, x2, 1, z2);
    }

    public boolean addParent(UUID ownerId, World world, BlockPos pos, BlockPos offsetPos, Direction currentFacing) {
        BlockState offsetState = world.getBlockState(offsetPos);
        if (offsetState.getBlock() == this && offsetState.get(FACING) == currentFacing) {
            TileEntity te = world.getTileEntity(offsetPos);
            if (te instanceof BillboardTileEntity) {
                BillboardTileEntity parent = (BillboardTileEntity) te;
                if (!parent.locked || parent.ownerId.equals(ownerId)) {
                    parent.addChild(pos);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction direction = context.getPlacementHorizontalFacing().getOpposite();
        return this.getDefaultState().with(FACING, direction);
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return new BillboardTileEntity();
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 0;
    }
}
