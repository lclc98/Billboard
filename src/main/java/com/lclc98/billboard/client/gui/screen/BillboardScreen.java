package com.lclc98.billboard.client.gui.screen;

import com.lclc98.billboard.Billboard;
import com.lclc98.billboard.block.BillboardTileEntity;
import com.lclc98.billboard.network.UpdateMessage;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BillboardScreen extends Screen {

    private static final Pattern PATTERN = Pattern.compile("https://i\\.imgur\\.com/(.*)\\.(png|jpeg)");

    protected TextFieldWidget commandTextField;
    private final BillboardTileEntity parent;
    private Button doneButton;

    public BillboardScreen(BillboardTileEntity parent) {
        super(new StringTextComponent("Billboard"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.commandTextField = new TextFieldWidget(this.font, this.width / 2 - 150, 50, 300, 20, new TranslationTextComponent("advMode.command"));
        this.commandTextField.setText(String.format("https://i.imgur.com/%s.png", this.parent.getTextureId()));
        this.children.add(this.commandTextField);

        this.doneButton = this.addButton(new Button(this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20, DialogTexts.GUI_DONE, (p_214187_1_) -> {
            Matcher matcher = PATTERN.matcher(this.commandTextField.getText());
            if (matcher.find()) {
                String id = matcher.group(1);
                this.parent.setTexture(id);
                Billboard.NETWORK.sendToServer(new UpdateMessage(id, this.parent.getPos()));
            } else {
                // TODO
            }
        }));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        this.commandTextField.render(matrixStack, mouseX, mouseY, partialTicks);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
