package com.lclc98.billboard.util;

import com.lclc98.billboard.block.BillboardBlock;
import com.lclc98.billboard.block.BillboardTileEntity;
import com.lclc98.billboard.client.gui.screen.BillboardScreen;
import com.mojang.math.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class RenderUtil {

    public static float getWidth(BillboardTileEntity billboard) {
        return billboard.maxWidth - billboard.minWidth + 1;
    }

    public static float getHeight(BillboardTileEntity billboard) {
        return billboard.maxHeight - billboard.minHeight + 1;
    }

    public static Vector4f getUV(Direction direction, BillboardTileEntity billboard, BlockPos pos) {

        float minU;
        float minV = (billboard.maxHeight - ((direction == Direction.UP||direction == Direction.DOWN ) ? pos.getZ() : pos.getY())) / RenderUtil.getHeight(billboard);
        float maxU;
        float maxV = (billboard.maxHeight - ((direction == Direction.UP ||direction == Direction.DOWN ) ? pos.getZ() : pos.getY()) + 1) / RenderUtil.getHeight(billboard);

        BlockState state = billboard.getBlockState();
        if (invert(state)) {
            minU = (billboard.maxWidth - getUWithDirection(state, pos)) / RenderUtil.getWidth(billboard);
            maxU = (billboard.maxWidth - getUWithDirection(state, pos) + 1) / RenderUtil.getWidth(billboard);
        } else {
            minU = (getUWithDirection(state, pos) - billboard.minWidth) / RenderUtil.getWidth(billboard);
            maxU = (getUWithDirection(state, pos) - billboard.minWidth + 1) / RenderUtil.getWidth(billboard);
        }

        if (direction == Direction.DOWN && billboard.rotation == 0) {
            float m = minV;
            minV = maxV;
            maxV = m;
        }
        Vector4f vector4f = new Vector4f(minU, minV, maxU, maxV);
        if (billboard.rotation == 90 || billboard.rotation == 270 || billboard.rotation == 180) {
            if (billboard.rotation != 180) {
                minV = (billboard.maxHeight - ((direction == Direction.UP || direction == Direction.DOWN) ? pos.getZ() : pos.getY()) + 1) / RenderUtil.getHeight(billboard);
                maxV = (billboard.maxHeight - ((direction == Direction.UP || direction == Direction.DOWN) ? pos.getZ() : pos.getY())) / RenderUtil.getHeight(billboard);
            }

            final float angle = (float) Math.toRadians(billboard.rotation);
            final float mid = 0.5f;
            float minUC = (float) (((minU - mid) * Math.cos(angle)) - ((mid - minV) * Math.sin(angle))) + mid;
            float minVC = (float) (mid - ((mid - minV) * Math.cos(angle) + (minU - mid) * Math.sin(angle)));


            float maxUC = (float) ((maxU - mid) * Math.cos(angle) - (mid - maxV) * Math.sin(angle)) + mid;
            float maxVC = (float) (mid - ((mid - maxV) * Math.cos(angle) + (maxU - mid) * Math.sin(angle)));

            vector4f.set(minUC, minVC, maxUC, maxVC);
        }
        return vector4f;
    }

    public static int getUWithDirection(BlockState state, BlockPos p) {
        Direction direction = state.getValue(BillboardBlock.FACING);
        if (direction == Direction.DOWN || direction == Direction.UP) {
            return p.getX();
        }
        return direction.getStepX() == 0 ? p.getX() : p.getZ();
    }

    public static void initWidthHeight(BillboardTileEntity te) {
        final BlockState state = te.getBlockState();
        final Direction direction = state.getValue(BillboardBlock.FACING);
        if (te.getChildren().isEmpty()) {
            te.minWidth = getUWithDirection(state, te.getBlockPos());
            te.minHeight = (direction == Direction.DOWN || direction == Direction.UP) ? te.getBlockPos().getZ() : te.getBlockPos().getY();
            te.maxWidth = getUWithDirection(state, te.getBlockPos());
            te.maxHeight = (direction == Direction.DOWN || direction == Direction.UP) ? te.getBlockPos().getZ() : te.getBlockPos().getY();
            te.uv = new Vector4f(0, 0, 1, 1);
            return;
        }
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (BlockPos childPos : te.getChildren()) {
            minX = Math.min(minX, getUWithDirection(state, childPos));
            maxX = Math.max(maxX, getUWithDirection(state, childPos));
            minY = Math.min(minY, (direction == Direction.DOWN || direction == Direction.UP) ? childPos.getZ() : childPos.getY());
            maxY = Math.max(maxY, (direction== Direction.DOWN || direction == Direction.UP) ? childPos.getZ() : childPos.getY());
        }
        te.minWidth = Math.min(minX, getUWithDirection(state, te.getBlockPos()));
        te.maxWidth = Math.max(maxX, getUWithDirection(state, te.getBlockPos()));
        te.minHeight = Math.min(minY, (direction == Direction.DOWN || direction == Direction.UP) ? te.getBlockPos().getZ() : te.getBlockPos().getY());
        te.maxHeight = Math.max(maxY, (direction == Direction.DOWN || direction == Direction.UP) ? te.getBlockPos().getZ() : te.getBlockPos().getY());
        te.uv = RenderUtil.getUV(direction, te, te.getBlockPos());
    }

    public static boolean invert(BlockState state) {
        Direction direction = state.getValue(BillboardBlock.FACING);
        return direction == Direction.NORTH || direction == Direction.EAST || direction == Direction.DOWN || direction == Direction.UP;
    }

    public static void openGui(BillboardTileEntity billboard) {
        Minecraft.getInstance().setScreen(new BillboardScreen(billboard.getParent()));
    }
}
