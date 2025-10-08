package com.pecantpie.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;

import static com.pecantpie.block.TaskBoardBlock.FACING;

public class TaskBoardRenderer implements BlockEntityRenderer<TaskBoardBlockEntity> {
    private static final int TEXT_COLOR = DyeColor.BLACK.getTextColor();
    private static final int LINE_HEIGHT = 10;
    private static final int MAX_WIDTH = 83;
    private static final int MAX_LINES = 4;
    private static final float TEXT_SCALE = 0.012f;

    private final Font font;

    public TaskBoardRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(TaskBoardBlockEntity taskBoardBlockEntity, float pPartialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int pPackedLight, int pPackedOverlay) {
        poseStack.pushPose();
        centerText(poseStack, taskBoardBlockEntity.getBlockState());

        // render Task text. This should fit nicely as long as the task's name is 52 or less characters.
        // The default item name limit in the anvil is 50, so we should stick to that.
        poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        renderText(taskBoardBlockEntity.getTaskName(), poseStack, multiBufferSource, pPackedLight, null, MAX_WIDTH, MAX_LINES);

        poseStack.popPose();
    }

    void centerText(PoseStack poseStack, BlockState state) {
        Vector3f offset = state.getValue(FACING).step().mul(0.5125f); // just over 1 total so that it barely sticks out
        poseStack.translate(offset.x + 0.5f, 0.5f, offset.z + 0.5f);
        poseStack.mulPose(Axis.YN.rotationDegrees(state.getValue(FACING).toYRot()));
    }

    void renderText(Component text, PoseStack poseStack, MultiBufferSource buffer, int packedLight, @Nullable Vec2 offset, int maxWidth, int maxLines) {
        poseStack.pushPose();

        offset = offset == null ? Vec2.ZERO : offset;

        int midline = maxLines * LINE_HEIGHT / 2;

//        for(int i1 = 0; i1 < 4; ++i1) {
        int line = 0;
        for(FormattedCharSequence formattedcharsequence : font.split(FormattedText.of(text.getString()), maxWidth)) {
            float f = (float)(-this.font.width(formattedcharsequence) / 2);

            this.font.drawInBatch(formattedcharsequence, f + offset.x, (float)(line * LINE_HEIGHT - midline) + offset.y, TEXT_COLOR, false, poseStack.last().pose(), buffer, Font.DisplayMode.POLYGON_OFFSET, 0, packedLight);
            line++;
            if (line >= maxLines)
                break;
        }

        poseStack.popPose();
    }

}
