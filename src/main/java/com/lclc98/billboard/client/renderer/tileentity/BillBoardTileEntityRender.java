package com.lclc98.billboard.client.renderer.tileentity;

import com.lclc98.billboard.Billboard;
import com.lclc98.billboard.block.BillboardBlock;
import com.lclc98.billboard.block.BillboardTileEntity;
import com.lclc98.billboard.util.RenderUtil;
import com.lclc98.billboard.util.TextureUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class BillBoardTileEntityRender implements BlockEntityRenderer<BillboardTileEntity> {

    private static final ResourceLocation TEXTURE_WHITE = new ResourceLocation(Billboard.MOD_ID, "textures/block/white.png");
    private final EntityRenderDispatcher entityRenderer;

    public BillBoardTileEntityRender(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.getEntityRenderer();
    }


    @Override
    public void render(BillboardTileEntity te, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        BillboardTileEntity billboard = te.getParent();
        if (billboard == null) {
            return;
        }

//        if (billboard.dirty) {
        RenderUtil.initWidthHeight(billboard);
        billboard.dirty = false;
//        }

//        if (te.dirty) {
        te.setUV(RenderUtil.getUV(billboard, te.getBlockPos()));
        te.dirty = false;
//        }

        Direction direction = te.getBlockState().getValue(BillboardBlock.FACING);
        matrixStackIn.pushPose();
        Matrix4f matrix = matrixStackIn.last().pose();

        matrixStackIn.translate(0.5F, 0.5F, 0.5F);

        matrixStackIn.translate(direction.getStepX() * 0.5F, direction.getStepY() * 0.5F, direction.getStepZ() * 0.5F);
        matrixStackIn.mulPose(direction.getOpposite().getRotation());
        if (billboard.rotation == 90) {
            matrixStackIn.mulPose(Quaternion.fromXYZDegrees(new Vector3f(0, -billboard.rotation, 0)));
        }

        if (billboard.rotation == 270) {
            matrixStackIn.mulPose(Quaternion.fromXYZDegrees(new Vector3f(0, billboard.rotation, 0)));
        }


        renderSquare(TEXTURE_WHITE, new Vector4f(0, 1, 0, 1), bufferIn, matrix, combinedOverlayIn, combinedLightIn);

        renderSquare(TextureUtil.getTexture(billboard), te.getUV(), bufferIn, matrix, combinedOverlayIn, combinedLightIn);
        matrixStackIn.popPose();
    }

    public void renderSquare(ResourceLocation texture, Vector4f uv, MultiBufferSource bufferIn, Matrix4f matrix, int combinedOverlayIn, int combinedLightIn) {
        VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.entityCutout(texture));
        final float f = 1.0f / 16.0f;

        ivertexbuilder.vertex(matrix, -0.5F, 1 - f - 0.001F, -0.5F).color(255, 255, 255, 255).uv(uv.z(), uv.y()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(0, 0, 1f).endVertex();
        ivertexbuilder.vertex(matrix, 0.5F, 1 - f - 0.001F, -0.5F).color(255, 255, 255, 255).uv(uv.x(), uv.y()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(0, 0, 1f).endVertex();
        ivertexbuilder.vertex(matrix, 0.5F, 1 - f - 0.001F, 0.5F).color(255, 255, 255, 255).uv(uv.x(), uv.w()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(0, 0, 1f).endVertex();
        ivertexbuilder.vertex(matrix, -0.5F, 1 - f - 0.001F, 0.5F).color(255, 255, 255, 255).uv(uv.z(), uv.w()).overlayCoords(combinedOverlayIn).uv2(combinedLightIn).normal(0, 0, 1f).endVertex();
    }
}