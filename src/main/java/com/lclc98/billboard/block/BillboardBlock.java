package com.lclc98.billboard.block;

import com.lclc98.billboard.util.RenderUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BillboardBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final DirectionProperty DIRECTION = BlockStateProperties.FACING;

    public BillboardBlock() {
        super(Properties.of(Material.WOOD).noCollission());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (level.isClientSide) {
            BlockEntity te = level.getBlockEntity(pos);
            if (te instanceof BillboardTileEntity) {
                BillboardTileEntity parent = ((BillboardTileEntity) te).getParent();
                if (parent.hasPermission(player)) {
                    RenderUtil.openGui(parent);
                } else {
                    player.displayClientMessage(Component.literal("You don't have permission to open this."), false);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @org.jetbrains.annotations.Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide || !(placer instanceof Player player)) {
            return;
        }

        if (!placer.isCrouching()) {
            Direction currentFacing = state.getValue(FACING);
            Direction dir = state.getValue(FACING);

            if (dir == Direction.UP || dir == Direction.DOWN) {
                if (this.addParent(player, level, pos, pos.relative(Direction.NORTH), currentFacing)) {
                    return;
                }
                if (this.addParent(player, level, pos, pos.relative(Direction.SOUTH), currentFacing)) {
                    return;
                }
                if (this.addParent(player, level, pos, pos.relative(Direction.EAST), currentFacing)) {
                    return;
                }
                if (this.addParent(player, level, pos, pos.relative(Direction.WEST), currentFacing)) {
                    return;
                }
            }else {
                if (this.addParent(player, level, pos, pos.relative(Direction.UP), currentFacing)) {
                    return;
                }
                if (this.addParent(player, level, pos, pos.relative(Direction.DOWN), currentFacing)) {
                    return;
                }

                if (this.addParent(player, level, pos, pos.relative(dir.getClockWise()), currentFacing)) {
                    return;
                }
                if (this.addParent(player, level, pos, pos.relative(dir.getCounterClockWise()), currentFacing)) {
                    return;
                }
            }
        }
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof BillboardTileEntity parent) {
            parent.ownerId = placer.getUUID();
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        if (!state.hasProperty(FACING)) {
            return Shapes.block();
        }
        final float f = 1.0f / 16.0f;
        Direction direction = state.getValue(FACING);

        float x1 = direction == Direction.WEST ? 1.0f - f : 0;
        float x2 = direction == Direction.EAST ? f : 1;
        float z1 = direction == Direction.NORTH ? 1.0f - f : 0;
        float z2 = direction == Direction.SOUTH ? f : 1;

        float y1 = direction == Direction.DOWN ? 1.0f - f : 0;
        float y2 = direction == Direction.UP ? f : 1;

        return Shapes.box(x1, y1, z1, x2, y2, z2);
    }

    public boolean addParent(Player player, Level level, BlockPos pos, BlockPos offsetPos, Direction currentFacing) {
        BlockState offsetState = level.getBlockState(offsetPos);
        if (offsetState.getBlock() == this && offsetState.getValue(FACING) == currentFacing) {
            BlockEntity be = level.getBlockEntity(offsetPos);
            if (be instanceof BillboardTileEntity parent) {
                if (parent.hasPermission(player)) {
                    parent.addChild(pos);
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public void onRemove(BlockState p_60515_, Level level, BlockPos pos, BlockState p_60518_, boolean p_60519_) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof BillboardTileEntity parent) {
            parent.blockBroken();
        }
        super.onRemove(p_60515_, level, pos, p_60518_, p_60519_);
    }

    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public int getLightBlock(BlockState p_60585_, BlockGetter p_60586_, BlockPos p_60587_) {
        return 0;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BillboardTileEntity(blockPos, blockState);
    }
}
