package com.lclc98.billboard.util;

import com.lclc98.billboard.block.BillboardBlock;
import com.lclc98.billboard.block.BillboardTileEntity;
import com.lclc98.billboard.client.gui.screen.BillboardScreen;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector4f;

public class RenderUtil {

    public static float getWidth(BillboardTileEntity billboard) {
        return billboard.maxWidth - billboard.minWidth + 1;
    }

    public static float getHeight(BillboardTileEntity billboard) {
        return billboard.maxHeight - billboard.minHeight + 1;
    }

    public static Vector4f getUV(BillboardTileEntity billboard, BlockPos pos) {
        float minU;
        float minV = (billboard.maxHeight - pos.getY()) / RenderUtil.getHeight(billboard);
        float maxU;
        float maxV = (billboard.maxHeight - pos.getY() + 1) / RenderUtil.getHeight(billboard);

        BlockState state = billboard.getBlockState();
        if (invert(state)) {
            minU = (billboard.maxWidth - getUWithDirection(state, pos)) / RenderUtil.getWidth(billboard);
            maxU = (billboard.maxWidth - getUWithDirection(state, pos) + 1) / RenderUtil.getWidth(billboard);
        } else {
            minU = (getUWithDirection(state, pos) - billboard.minWidth) / RenderUtil.getWidth(billboard);
            maxU = (getUWithDirection(state, pos) - billboard.minWidth + 1) / RenderUtil.getWidth(billboard);
        }

        return new Vector4f(minU, minV, maxU, maxV);
    }

    public static int getUWithDirection(BlockState state, BlockPos p) {
        Direction direction = state.getValue(BillboardBlock.FACING);
        return direction.getStepX() == 0 ? p.getX() : p.getZ();
    }

    public static void initWidthHeight(BillboardTileEntity te) {
        final BlockState state = te.getBlockState();
        if (te.getChildren().isEmpty()) {
            te.minWidth = getUWithDirection(state, te.getBlockPos());
            te.minHeight = te.getBlockPos().getY();
            te.maxWidth = getUWithDirection(state, te.getBlockPos());
            te.maxHeight = te.getBlockPos().getY();
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
            minY = Math.min(minY, childPos.getY());
            maxY = Math.max(maxY, childPos.getY());
        }
        te.minWidth = Math.min(minX, getUWithDirection(state, te.getBlockPos()));
        te.maxWidth = Math.max(maxX, getUWithDirection(state, te.getBlockPos()));
        te.minHeight = Math.min(minY, te.getBlockPos().getY());
        te.maxHeight = Math.max(maxY, te.getBlockPos().getY());
        te.uv = RenderUtil.getUV(te, te.getBlockPos());
    }

    public static boolean invert(BlockState state) {
        return state.getValue(BillboardBlock.FACING) == Direction.NORTH || state.getValue(BillboardBlock.FACING) == Direction.EAST;
    }

    public static void openGui(BillboardTileEntity billboard) {
        Minecraft.getInstance().setScreen(new BillboardScreen(billboard.getParent()));
    }
}
