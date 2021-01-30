package com.lclc98.billboard.client.renderer.tileentity;

import com.lclc98.billboard.Billboard;
import com.lclc98.billboard.block.BillboardBlock;
import com.lclc98.billboard.block.BillboardTileEntity;
import com.lclc98.billboard.util.RenderUtil;
import com.lclc98.billboard.util.TextureUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector4f;

public class BillBoardTileEntityRender extends TileEntityRenderer<BillboardTileEntity> {

    private static final ResourceLocation TEXTURE_WHITE = new ResourceLocation(Billboard.MOD_ID, "textures/block/white.png");

    public BillBoardTileEntityRender(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(BillboardTileEntity te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        BillboardTileEntity billboard = te.getParent();
        if (billboard == null) {
            return;
        }

        if (billboard.dirty) {
            RenderUtil.initWidthHeight(billboard);
            billboard.dirty = false;
        }

        if (te.dirty) {
            te.setUV(RenderUtil.getUV(billboard, te.getPos()));
            te.dirty = false;
        }

        Direction direction = te.getBlockState().get(BillboardBlock.FACING);
        matrixStackIn.push();
        Matrix4f matrix = matrixStackIn.getLast().getMatrix();
        matrixStackIn.translate(0.5F, 0.5F, 0.5F);
        matrixStackIn.translate(direction.getXOffset() * 0.5F, 0, direction.getZOffset() * 0.5F);
        matrixStackIn.rotate(direction.getOpposite().getRotation());

        renderSquare(TEXTURE_WHITE, new Vector4f(0, 1, 0, 1), bufferIn, matrix, combinedOverlayIn, combinedLightIn);

        renderSquare(TextureUtil.getTexture(billboard), te.getUV(), bufferIn, matrix, combinedOverlayIn, combinedLightIn);
        matrixStackIn.pop();
    }

    public void renderSquare(ResourceLocation texture, Vector4f uv, IRenderTypeBuffer bufferIn, Matrix4f matrix, int combinedOverlayIn, int combinedLightIn) {
        IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getEntityCutout(texture));
        float f = 1.0f / 16.0f;

        ivertexbuilder.pos(matrix, 0.5F, 1 - f - 0.001F, -0.5F).color(255, 255, 255, 255).tex(uv.getX(), uv.getY()).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(0, 0, 1f).endVertex();
        ivertexbuilder.pos(matrix, 0.5F, 1 - f - 0.001F, 0.5F).color(255, 255, 255, 255).tex(uv.getX(), uv.getW()).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(0, 0, 1f).endVertex();
        ivertexbuilder.pos(matrix, -0.5F, 1 - f - 0.001F, 0.5F).color(255, 255, 255, 255).tex(uv.getZ(), uv.getW()).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(0, 0, 1f).endVertex();
        ivertexbuilder.pos(matrix, -0.5F, 1 - f - 0.001F, -0.5F).color(255, 255, 255, 255).tex(uv.getZ(), uv.getY()).overlay(combinedOverlayIn).lightmap(combinedLightIn).normal(0, 0, 1f).endVertex();
    }
}