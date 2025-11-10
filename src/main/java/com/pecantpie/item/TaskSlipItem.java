package com.pecantpie.item;

import com.pecantpie.ProjectBoardData;
import com.pecantpie.ProjectBoards;
import com.pecantpie.component.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TaskSlipItem extends Item {
    public TaskSlipItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {

        String ownerName = stack.get(ModDataComponents.OWNER_NAME);
//        ProjectBoards.LOGGER.debug("Task slip owner name: " + ownerName);
        if (ownerName != null) {
            tooltipComponents.add(Component.literal(ownerName));
        }

        Component statusName = getTaskStatusComponent(stack);
        tooltipComponents.add(statusName);

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @NotNull
    // This returns a Component so the statuses can be easily localized.
    // Localizations are at "item.projectboards.task_slip.*"
    public static Component getTaskStatusComponent(ItemStack item) {
        Short status = getRawStatus(item);

        if (status != null) {
            return ProjectBoardData.TaskStatus.fromShort(status).getTranslatableComponent();
        }
        // if no status is found, return empty
        return Component.empty();
    }

    @Nullable
    public static Short getRawStatus(ItemStack item) {
        if (!item.isEmpty()) {
            return item.get(ModDataComponents.STATUS_CODE);
        } else {
            return null;
        }
    }

    public static void incrementTaskStatus(ItemStack item) {
        if (!item.isEmpty()) {
            Short statusCode = getRawStatus(item);
            if (statusCode != null) {
                Short newCode = ProjectBoardData.TaskStatus.toShort((ProjectBoardData.TaskStatus.fromShort(statusCode)).next());
                item.set(ModDataComponents.STATUS_CODE, newCode);
            } else {
                resetTaskStatus(item);
            }
        }
    }

    public static void decrementTaskStatus(ItemStack item) {
        if (!item.isEmpty()) {
            Short statusCode = getRawStatus(item);
            if (statusCode != null) {
                Short newCode = ProjectBoardData.TaskStatus.toShort((ProjectBoardData.TaskStatus.fromShort(statusCode)).previous());
                item.set(ModDataComponents.STATUS_CODE, newCode);
            } else {
                resetTaskStatus(item);
            }
        }
    }

    public static void resetTaskStatus(ItemStack item) {
        if (!item.isEmpty()) {
            item.set(ModDataComponents.STATUS_CODE, (short)0);
        }
    }

}
