package com.pecantpie.item;

import com.pecantpie.ProjectBoards;
import com.pecantpie.component.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

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

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
