package com.lclc98.billboard.client.gui.screen;

import com.lclc98.billboard.Billboard;
import com.lclc98.billboard.block.BillboardTileEntity;
import com.lclc98.billboard.network.UpdateMessage;
import com.lclc98.billboard.util.TextureUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;

public class BillboardScreen extends Screen {

    protected EditBox commandTextField;
    protected Checkbox checkboxButton;
    private final BillboardTileEntity parent;
    private Button doneButton;
    private String message = null;
    private int rotation;

    public BillboardScreen(BillboardTileEntity parent) {
        super(Component.literal("Edit Billboard"));
        this.parent = parent;
        this.rotation = parent.rotation;
    }

    @Override
    protected void init() {
        this.commandTextField = new EditBox(this.font, this.width / 2 - 150, this.height / 2 - 40, 300, 20, Component.translatable("advMode.command"));
        this.commandTextField.setMaxLength(256);
        this.commandTextField.setValue(this.parent.getTextureUrl());
        this.checkboxButton = new Checkbox(this.width / 2 - 75, this.height / 2 - 15, 20, 20, Component.literal("Lock to owner"), parent.locked, true);

        this.checkboxButton.active = this.minecraft.player.getUUID() == this.parent.ownerId;
        this.addWidget(this.commandTextField);

        this.addRenderableWidget(CycleButton.builder(Rotation::getDisplayName).withValues(Rotation.values()).withInitialValue(Rotation.getRotation(this.rotation)).create(this.width / 2 - 50, this.height / 2 , 100, 20, Component.literal("Rotation"), (p_193854_, rot) -> {
            this.rotation = rot.rotation;
        }));

        this.doneButton = this.addRenderableWidget(new Button(this.width / 2 - 75, this.height / 2 + 40, 150, 20, CommonComponents.GUI_DONE, (p_214187_1_) -> {
            String textureUrl = this.commandTextField.getValue();
            if (TextureUtil.validateUrl(textureUrl)) {
                Billboard.NETWORK.sendToServer(new UpdateMessage(this.parent.getBlockPos(), textureUrl, this.checkboxButton.selected(), rotation));
                this.onClose();
            } else {
                this.message = "Invalid url, Imgur link only e.g. https://i.imgur.com/DHHCsdx.png (or jpg, or jpeg)";
            }
        }));
    }


    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(poseStack);
        this.commandTextField.render(poseStack, mouseX, mouseY, partialTicks);

        drawCenteredString(poseStack, this.font, this.title, this.width / 2, 10, 16777215);

        if (!StringUtils.isEmpty(this.message)) {
            drawCenteredString(poseStack, this.font, this.message, this.width / 2, this.height / 2 - 30, 16777215);
        }
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    enum Rotation {
        ROT_0(0),
        ROT_90(90),
        ROT_180(180),
        ROT_270(270);

        private final int rotation;

        Rotation(int rotation) {
            this.rotation = rotation;
        }

        public Component getDisplayName() {
            return Component.literal(String.valueOf(this.rotation));
        }

        public static Rotation getRotation(int rotation) {
            return switch (rotation) {
                case 90 -> ROT_90;
                case 180 -> ROT_180;
                case 270 -> ROT_270;
                default -> ROT_0;
            };
        }


    }
}
