package com.lclc98.billboard.client.gui.screen;

import com.lclc98.billboard.Billboard;
import com.lclc98.billboard.block.BillboardTileEntity;
import com.lclc98.billboard.network.UpdateMessage;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BillboardScreen extends Screen {

    private static final Pattern PATTERN = Pattern.compile("^https://i\\.imgur\\.com/(.*)\\.(png|jpg|jpeg)$");

    protected TextFieldWidget commandTextField;
    protected CheckboxButton checkboxButton;
    private final BillboardTileEntity parent;
    private Button doneButton;
    private String message = null;

    public BillboardScreen(BillboardTileEntity parent) {
        super(new StringTextComponent("Edit Billboard"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.commandTextField = new TextFieldWidget(this.font, this.width / 2 - 150, this.height / 2 - 40, 300, 20, new TranslationTextComponent("advMode.command"));
        this.commandTextField.setText(String.format("https://i.imgur.com/%s.png", this.parent.getTextureId()));
        this.checkboxButton = new CheckboxButton(this.width / 2 - 75, this.height / 2 - 15, 20, 20, new StringTextComponent("Lock to owner"), parent.locked, true);

        this.checkboxButton.active = this.minecraft.player.getUniqueID() == this.parent.ownerId;
        this.children.add(this.commandTextField);
        this.addButton(this.checkboxButton);

        this.doneButton = this.addButton(new Button(this.width / 2 - 75, this.height / 2 + 10, 150, 20, DialogTexts.GUI_DONE, (p_214187_1_) -> {
            Matcher matcher = PATTERN.matcher(this.commandTextField.getText());
            if (matcher.find()) {
                String id = matcher.group(1);
                Billboard.NETWORK.sendToServer(new UpdateMessage(this.parent.getPos(), id, this.checkboxButton.isChecked()));
                this.closeScreen();
            } else {
                this.message = "Invalid url, Imgur link only e.g. https://i.imgur.com/DHHCsdx.png (or jpg, or jpeg)";
            }
        }));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        this.commandTextField.render(matrixStack, mouseX, mouseY, partialTicks);

        drawCenteredString(matrixStack, this.font, this.title, this.width / 2, 10, 16777215);

        if (!StringUtils.isNullOrEmpty(this.message)) {
            drawCenteredString(matrixStack, this.font, this.message, this.width / 2, this.height / 2 - 30, 16777215);
        }
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
