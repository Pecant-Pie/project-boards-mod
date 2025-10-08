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
import net.minecraft.util.ColorRGBA;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

import static com.pecantpie.block.TaskBoardBlock.FACING;

public class TaskBoardRenderer implements BlockEntityRenderer<TaskBoardBlockEntity> {
    private static final int TEXT_COLOR = DyeColor.BLACK.getTextColor();
    private static final int LINE_HEIGHT = 10;
    private static final int MAX_WIDTH = 90;
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
        poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        renderText(taskBoardBlockEntity.getBlockPos(), taskBoardBlockEntity.getTaskName(), poseStack, multiBufferSource, pPackedLight);
        poseStack.popPose();
    }
    
    void centerText(PoseStack poseStack, BlockState state) {
        Vector3f offset = state.getValue(FACING).step().mul(0.5125f); // just over 1 total so that it barely sticks out
        poseStack.translate(offset.x + 0.5f, 0.5f, offset.z + 0.5f);
        poseStack.mulPose(Axis.YN.rotationDegrees(state.getValue(FACING).toYRot()));
    }

    void renderText(BlockPos pos, Component text, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        int j = MAX_LINES * LINE_HEIGHT / 2;
        FormattedCharSequence aformattedcharsequence = text.getVisualOrderText();


//        for(int i1 = 0; i1 < 4; ++i1) {
//            FormattedCharSequence formattedcharsequence = aformattedcharsequence;
            float f = (float)(-this.font.width(aformattedcharsequence) / 2);

            this.font.drawInBatch(aformattedcharsequence, f, (float)(0 * LINE_HEIGHT - j), TEXT_COLOR, false, poseStack.last().pose(), buffer, Font.DisplayMode.POLYGON_OFFSET, 0, packedLight);
//        }

        poseStack.popPose();
    }

}
