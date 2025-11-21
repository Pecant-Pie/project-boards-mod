package com.pecantpie.screen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.pecantpie.ProjectBoardData;
import com.pecantpie.ProjectBoards;
import com.pecantpie.block.TaskBoardBlockEntity;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;

/// Lots of the code here was inspired / taken from the Minecraft code for AbstractSignEditScreen

public class TaskBoardScreen extends Screen implements MenuAccess<TaskBoardMenu> {
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ProjectBoards.MODID, "textures/gui/task_board/task_board_edit_screen.png");
    private static final int bgWidth = 160;
    private static final int bgHeight = 160;

    private final TaskBoardBlockEntity tbbe;
    private final TaskBoardMenu menu;

    private String currentName = null;

    @Nullable
    private TextFieldHelper taskNameHelper;
    private int frame = 0;


    public TaskBoardScreen(TaskBoardMenu menu, Inventory playerInventory, Component title) {
        super(title);
        this.menu = menu;
        this.tbbe = menu.blockEntity;
        this.currentName = tbbe.getTaskString();
    }

    protected void offsetTask(GuiGraphics guiGraphics) {
        guiGraphics.pose().translate((float)this.width / 2.0F, 80.0F, 50.0F);
    }


    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

//        int x = (width - bgWidth) / 2;
//        int y = (height - bgHeight) / 4;

        guiGraphics.blit(GUI_TEXTURE, -80, -40, 0, 0, 160, 160);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        offsetTask(guiGraphics);
        renderBg(guiGraphics, partialTick, mouseX, mouseY);
        Lighting.setupForFlatItems();
        guiGraphics.drawCenteredString(this.font, this.title, 0, -60, 16777215);
        this.renderTask(guiGraphics);
        Lighting.setupFor3DItems();
    }

    private void renderTask(GuiGraphics guiGraphics) {
        guiGraphics.pose().pushPose();
        this.renderTaskText(guiGraphics);
        guiGraphics.pose().popPose();
    }

    private void renderTaskText(GuiGraphics guiGraphics) {
        guiGraphics.pose().translate(0.0F, 60.0F, 4.0F);
        float textScale = getTaskTextScale();
        guiGraphics.pose().scale(textScale, textScale, textScale);
        int textColor = this.getTextColor();
        boolean blink = this.frame / 6 % 2 == 0;


        String[] lines = splitStringUpTo4(currentName);

        int truePosition = this.taskNameHelper.getCursorPos();
        int posInLine = this.getPosInLine(truePosition, lines);
        int trueMarkPosition = this.taskNameHelper.getSelectionPos();
        int markPosInLine = this.getPosInLine(trueMarkPosition, lines);
        int markLine = this.getLineFromPos(trueMarkPosition, lines);
        int line = this.getLineFromPos(truePosition, lines);
        int yOffset = 4 * this.getTextLineHeight() / 2;
        int selectedLineY = line * this.getTextLineHeight() - yOffset;


        for(int j1 = 0; j1 < lines.length; ++j1) {
            String s = lines[j1];
            if (s != null) {
                if (this.font.isBidirectional()) {
                    s = this.font.bidirectionalShaping(s);
                }

                int k1 = -this.font.width(s) / 2;
                guiGraphics.drawString(this.font, s, k1, j1 * this.getTextLineHeight() - yOffset, textColor, false);
                // draw blinking _
                if (j1 == line && posInLine >= 0 && blink) {
                    int l1 = this.font.width(s.substring(0, Math.max(Math.min(posInLine, s.length()), 0)));
                    int i2 = l1 - this.font.width(s) / 2;
                    if (posInLine >= s.length()) {
                        guiGraphics.drawString(this.font, "_", i2, selectedLineY, textColor, false);
                    }
                }
            }
        }

        boolean isMarkFirst = trueMarkPosition < truePosition;

        int topLine = Math.min(markLine, line);
        int bottomLine = Math.max(markLine, line);


        // draw blinking | if cursor is between characters
        int l3 = this.font.width(lines[line].substring(0, Math.max(Math.min(posInLine, lines[line].length()), 0)));
        int i4 = l3 - this.font.width(lines[line]) / 2;
        if (blink && posInLine < lines[line].length()) {
            guiGraphics.fill(i4, selectedLineY - 1, i4 + 1, selectedLineY + this.getTextLineHeight(), -16777216 | textColor);
        }

        for(int highlightI = 0; highlightI < lines.length; ++highlightI) {
            String lineToHighlight = lines[highlightI];
            if (trueMarkPosition != truePosition && lineToHighlight != null && highlightI >= topLine && highlightI <= bottomLine) {

                int highlightedLineY = highlightI * this.getTextLineHeight() - yOffset;
                int leftMostEdgePos = highlightI > topLine ? 0 : (isMarkFirst ? markPosInLine : posInLine);
                int rightMostEdgePos = highlightI < bottomLine ? lineToHighlight.length() : (isMarkFirst ? posInLine : markPosInLine);
                int leftEdgeOffset = this.font.width(lineToHighlight.substring(0, leftMostEdgePos)) - this.font.width(lineToHighlight) / 2;
                int rightEdgeOffset = this.font.width(lineToHighlight.substring(0, rightMostEdgePos)) - this.font.width(lineToHighlight) / 2;
                int i3 = Math.min(leftEdgeOffset, rightEdgeOffset);
                int j3 = Math.max(leftEdgeOffset, rightEdgeOffset);
                guiGraphics.fill(RenderType.guiTextHighlight(), leftEdgeOffset, highlightedLineY, rightEdgeOffset, highlightedLineY + this.getTextLineHeight(), -16776961);
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
    private float getTaskTextScale() {
        return 1.75F;
    }

    private int getLineFromPos(int position, String[] lines) {
        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            if (Math.min(position, lines[lineIndex].length()) == position) {
                return lineIndex;
            } else {
                position -= lines[lineIndex].length() +
                        // count an extra character if the line ends in a space
                        (endsInWhiteSpace(lines[lineIndex]) ? 1 : 0);
            }
        }
        return lines.length - 1;
    }

    private int getPosInLine(int position, String[] lines) {
        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            if (Math.min(position, lines[lineIndex].length()) == position) {
                return position;
            } else {
                position -= lines[lineIndex].stripTrailing().length() +
                        // count an extra character if the line ends in a space
                        (endsInWhiteSpace(lines[lineIndex]) ? 1 : 0);
            }
        }
        return lines[lines.length - 1].length();
    }

    protected boolean endsInWhiteSpace(String line) {
        return line.endsWith(" ");
    }


    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
//        super.renderLabels(guiGraphics, mouseX, mouseY);
    }


    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode != 264 && keyCode != 265 && keyCode != 257 && keyCode != 335) {
            return this.taskNameHelper.keyPressed(keyCode) ? true : super.keyPressed(keyCode, scanCode, modifiers);
        } else if (keyCode == 265) { // up arrow key
            if (modifiers == 1 && taskNameHelper.getCursorPos() == taskNameHelper.getSelectionPos()) {
                this.taskNameHelper.setSelectionPos(taskNameHelper.getCursorPos());
            }
            this.taskNameHelper.setCursorToStart(modifiers == 1);
            return true;
        }
        else {
            if (modifiers == 1 && taskNameHelper.getCursorPos() == taskNameHelper.getSelectionPos()) {
                this.taskNameHelper.setSelectionPos(taskNameHelper.getCursorPos());
            }
            this.taskNameHelper.setCursorToEnd(modifiers == 1);
            return true;
        }
    }

    public String[] splitStringUpTo4(String toSplit) {
        StringSplitter splitter = font.getSplitter();

        String stringLeft = toSplit;
        // HARDCODED NUMBER OF LINES FOR TASK EDIT SCREEN
        String[] lines = {"","","",""};
        for (int ind = 0; ind < lines.length; ind++) {
            int lineBreak = splitter.findLineBreak(stringLeft, getMaxTextWidth(), Style.EMPTY);
            // line break will equal 0 if none was found
            if (lineBreak <= 0) {
                lines[ind] = splitter.plainHeadByWidth(stringLeft, getMaxTextWidth(), Style.EMPTY);
            } else {
                lines[ind] = stringLeft.substring(0, lineBreak);
            }
            stringLeft = stringLeft.substring(lines[ind].length());
        }
        return lines;
    }

    public void removed() {
        PacketDistributor.sendToServer(new ProjectBoardData.EditTaskData(currentName));
    }

    public boolean charTyped(char codePoint, int modifiers) {
        this.taskNameHelper.charTyped(codePoint);
        return true;
    }

    protected void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE,
                (p_251194_) -> this.onDone()).bounds(this.width / 2 - 100, this.height / 4 + 144, 200, 20).build());
        this.taskNameHelper = new TextFieldHelper(this::getTaskString, this::setTaskString, TextFieldHelper.createClipboardGetter(this.minecraft), TextFieldHelper.createClipboardSetter(this.minecraft), tbbe::isTaskNameValid);
    }

    public void tick() {
        ++this.frame;
    }

    private void onDone() {
        this.minecraft.setScreen(null);
    }

    private String getTaskString() {
        return currentName;
    }

    private void setTaskString(String taskName) {
        currentName = taskName;
    }

    @Override
    public TaskBoardMenu getMenu() {
        return this.menu;
    }
}
