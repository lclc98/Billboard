package com.lclc98.billboard.client.gui.screen;

import com.lclc98.billboard.Billboard;
import com.lclc98.billboard.block.BillboardTileEntity;
import com.lclc98.billboard.network.UpdateMessage;
import com.lclc98.billboard.util.TextureUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
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

    public BillboardScreen(BillboardTileEntity parent) {
        super(Component.literal("Edit Billboard"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.commandTextField = new EditBox(this.font, this.width / 2 - 150, this.height / 2 - 40, 300, 20, Component.translatable("advMode.command"));
        this.commandTextField.setValue(this.parent.getTextureUrl());
        this.checkboxButton = new Checkbox(this.width / 2 - 75, this.height / 2 - 15, 20, 20, Component.literal("Lock to owner"), parent.locked, true);

        this.checkboxButton.active = this.minecraft.player.getUUID() == this.parent.ownerId;
        this.addWidget(this.commandTextField);
//        this.addButton(this.checkboxButton);

        this.doneButton = this.addRenderableWidget(new Button(this.width / 2 - 75, this.height / 2 + 10, 150, 20, CommonComponents.GUI_DONE, (p_214187_1_) -> {
            String textureUrl = this.commandTextField.getValue();
            if (TextureUtil.validateUrl(textureUrl)) {
                Billboard.NETWORK.sendToServer(new UpdateMessage(this.parent.getBlockPos(), textureUrl, this.checkboxButton.selected()));
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
}
