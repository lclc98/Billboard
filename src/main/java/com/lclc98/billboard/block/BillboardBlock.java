package com.lclc98.billboard.block;

import com.lclc98.billboard.Billboard;
import com.lclc98.billboard.client.video.VideoDisplay;
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

public class BillboardBlock extends ContainerBlock {
    public static final DirectionProperty FACING = HorizontalBlock.FACING;

    public BillboardBlock() {
        super(Properties.of(Material.WOOD).noCollission());
        this.setRegistryName(new ResourceLocation(Billboard.MOD_ID, "billboard"));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isClientSide) {
            TileEntity te = worldIn.getBlockEntity(pos);
            if (te instanceof BillboardTileEntity) {
                BillboardTileEntity parent = ((BillboardTileEntity) te).getParent();
                if (parent.hasPermission(player)) {
                    RenderUtil.openGui(parent);
                } else {
                    player.displayClientMessage(new StringTextComponent("You don't have permission to open this."), false);
                }
            }
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!(placer instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity uuid = (PlayerEntity) placer;

        if (!placer.isCrouching()) {
            Direction currentFacing = state.getValue(FACING);

            if (this.addParent(uuid, worldIn, pos, pos.relative(Direction.UP), currentFacing)) {
                return;
            }
            if (this.addParent(uuid, worldIn, pos, pos.relative(Direction.DOWN), currentFacing)) {
                return;
            }
            if (this.addParent(uuid, worldIn, pos, pos.relative(state.getValue(FACING).getClockWise()), currentFacing)) {
                return;
            }
            if (this.addParent(uuid, worldIn, pos, pos.relative(state.getValue(FACING).getCounterClockWise()), currentFacing)) {
                return;
            }
        }
        TileEntity te = worldIn.getBlockEntity(pos);
        if (te instanceof BillboardTileEntity) {
            BillboardTileEntity parent = (BillboardTileEntity) te;
            parent.ownerId = placer.getUUID();
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        if (!state.hasProperty(FACING)) {
            return VoxelShapes.block();
        }
        final float f = 1.0f / 16.0f;
        Direction direction = state.getValue(FACING);

        float x1 = direction == Direction.WEST ? 1.0f - f : 0;
        float x2 = direction == Direction.EAST ? f : 1;
        float z1 = direction == Direction.NORTH ? 1.0f - f : 0;
        float z2 = direction == Direction.SOUTH ? f : 1;
        return VoxelShapes.box(x1, 0, z1, x2, 1, z2);
    }

    public boolean addParent(PlayerEntity player, World world, BlockPos pos, BlockPos offsetPos, Direction currentFacing) {
        BlockState offsetState = world.getBlockState(offsetPos);
        if (offsetState.getBlock() == this && offsetState.getValue(FACING) == currentFacing) {
            TileEntity te = world.getBlockEntity(offsetPos);
            if (te instanceof BillboardTileEntity) {
                BillboardTileEntity parent = (BillboardTileEntity) te;
                if (parent.hasPermission(player)) {
                    parent.addChild(pos);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction direction = context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, direction);
    }

    @Override
    public TileEntity newBlockEntity(IBlockReader worldIn) {
        return new BillboardTileEntity();
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public int getLightBlock(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 0;
    }
}
