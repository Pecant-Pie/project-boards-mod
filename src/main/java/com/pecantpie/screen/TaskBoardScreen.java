package com.pecantpie.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.pecantpie.ProjectBoards;
import com.pecantpie.block.TaskBoardBlockEntity;
import com.pecantpie.block.TaskBoardRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;

/// Lots of the code here was inspired / taken from the Minecraft code for AbstractSignEditScreen
/// TODO: Figure out why the changes aren't being saved on close! (probably server isn't receiving the changes)
public class TaskBoardScreen extends AbstractContainerScreen<TaskBoardMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ProjectBoards.MODID, "textures/gui/task_board/task_board_edit_screen.png");
    private static final int bgWidth = 160;
    private static final int bgHeight = 160;

    private final TaskBoardBlockEntity tbbe;

    @Nullable
    private TextFieldHelper taskNameHelper;
    private int frame = 0;


    public TaskBoardScreen(TaskBoardMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.tbbe = menu.blockEntity;
    }


    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - bgWidth) / 2;
        int y = (height - bgHeight) / 4;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, 160, 160);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        Lighting.setupForFlatItems();
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 40, 16777215);
        this.renderTask(guiGraphics);
        Lighting.setupFor3DItems();
    }

    private void renderTask(GuiGraphics guiGraphics) {
        guiGraphics.pose().pushPose();
        this.renderTaskText(guiGraphics);
        guiGraphics.pose().popPose();
    }

    private void renderTaskText(GuiGraphics guiGraphics) {
        guiGraphics.pose().translate(0.0F, 0.0F, 4.0F);
        Vector3f vector3f = this.getTaskTextScale();
        guiGraphics.pose().scale(vector3f.x(), vector3f.y(), vector3f.z());
        int i = this.getTextColor();
        boolean blink = this.frame / 6 % 2 == 0;

        List<FormattedCharSequence> formattedLines = font.split(FormattedText.of(tbbe.getTaskString()), this.getMaxTextWidth());
        String[] lines = new String[formattedLines.size()];
        // convert to lines
        for (int listI = 0; listI < formattedLines.size(); listI++) {
            lines[listI] = formattedLines.get(listI).toString();
        }


        int truePosition = this.taskNameHelper.getCursorPos();
        int j = this.getPosInLine(truePosition, lines);
        int trueSelectionPosition = this.taskNameHelper.getSelectionPos();
        int k = this.getPosInLine(trueSelectionPosition, lines);
        int line = this.getLineFromPos(truePosition, lines);
        int l = 4 * this.getTextLineHeight() / 2;
        int i1 = line * this.getTextLineHeight() - l;

        for(int j1 = 0; j1 < lines.length; ++j1) {
            String s = lines[j1];
            if (s != null) {
                if (this.font.isBidirectional()) {
                    s = this.font.bidirectionalShaping(s);
                }

                int k1 = -this.font.width(s) / 2;
                guiGraphics.drawString(this.font, s, k1, j1 * this.getTextLineHeight() - l, i, false);
                if (j1 == line && j >= 0 && blink) {
                    int l1 = this.font.width(s.substring(0, Math.max(Math.min(j, s.length()), 0)));
                    int i2 = l1 - this.font.width(s) / 2;
                    if (j >= s.length()) {
                        guiGraphics.drawString(this.font, "_", i2, i1, i, false);
                    }
                }
            }
        }

        for(int k3 = 0; k3 < lines.length; ++k3) {
            String s1 = lines[k3];
            if (s1 != null && k3 == line && j >= 0) {
                int l3 = this.font.width(s1.substring(0, Math.max(Math.min(j, s1.length()), 0)));
                int i4 = l3 - this.font.width(s1) / 2;
                if (blink && j < s1.length()) {
                    guiGraphics.fill(i4, i1 - 1, i4 + 1, i1 + this.getTextLineHeight(), -16777216 | i);
                }

                if (k != j) {
                    int j4 = Math.min(j, k);
                    int j2 = Math.max(j, k);
                    int k2 = this.font.width(s1.substring(0, j4)) - this.font.width(s1) / 2;
                    int l2 = this.font.width(s1.substring(0, j2)) - this.font.width(s1) / 2;
                    int i3 = Math.min(k2, l2);
                    int j3 = Math.max(k2, l2);
                    guiGraphics.fill(RenderType.guiTextHighlight(), i3, i1, j3, i1 + this.getTextLineHeight(), -16776961);
                }
            }
        }

    }

    ///  this is just how it is
    private int getTextLineHeight() {
        return 10;
    }

    ///  same as TaskBoardRenderer.MAX_WIDTH \
    /// *(i know this is generally bad code style but this might need to be different anyway)*
    private int getMaxTextWidth() {
        return 83;
    }

    private int getTextColor() {
        return DyeColor.BLACK.getTextColor();
    }

    ///  magic vector from Minecraft's SignEditScreen implementation
    private Vector3f getTaskTextScale() {
        return new Vector3f(0.9765628F, 0.9765628F, 0.9765628F);
    }

    private int getLineFromPos(int position, String[] lines) {
        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            if (Math.min(position, lines[lineIndex].length()) == position) {
                return lineIndex;
            } else {
                position -= lines[lineIndex].length();
            }
        }
        return lines.length - 1;
    }

    private int getPosInLine(int position, String[] lines) {
        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            if (Math.min(position, lines[lineIndex].length()) == position) {
                return position;
            } else {
                position -= lines[lineIndex].length();
            }
        }
        return lines[lines.length - 1].length();
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
//        super.renderLabels(guiGraphics, mouseX, mouseY);
    }


    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode != 264 && keyCode != 257 && keyCode != 335) {
            return this.taskNameHelper.keyPressed(keyCode) ? true : super.keyPressed(keyCode, scanCode, modifiers);
        } else {
            this.taskNameHelper.setCursorToEnd();
            return true;
        }
    }

    public boolean charTyped(char codePoint, int modifiers) {
        this.taskNameHelper.charTyped(codePoint);
        return true;
    }

    protected void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE,
                (p_251194_) -> this.onDone()).bounds(this.width / 2 - 100, this.height / 4 + 144, 200, 20).build());
        this.taskNameHelper = new TextFieldHelper(tbbe::getTaskString, tbbe::setTaskName, TextFieldHelper.createClipboardGetter(this.minecraft), TextFieldHelper.createClipboardSetter(this.minecraft), tbbe::isTaskNameValid);
    }

    public void containerTick() {
        ++this.frame;
    }

    private void onDone() {
        this.minecraft.setScreen(null);
    }
}
