package com.pecantpie.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jetbrains.annotations.NotNull;

public class TaskBoardBlockEntity extends BlockEntity implements ContainerSingleItem.BlockContainerSingleItem {

    private ItemStack item;

    public TaskBoardBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        item = ItemStack.EMPTY;
    }

    @Override
    public @NotNull BlockEntity getContainerBlockEntity() {
        return this;
    }

    @Override
    public @NotNull ItemStack getTheItem() {
        return item;
    }

    @Override
    public void setTheItem(@NotNull ItemStack itemStack) {
        item = itemStack;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.is(Items.PAPER) && stack.getCount() == 1 && BlockContainerSingleItem.super.canPlaceItem(slot, stack);
    }
}
