package com.pecantpie.item;

import com.pecantpie.block.TaskBoardBlock;
import com.pecantpie.block.TaskBoardBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class TaskBoardItem extends BlockItem {
    public TaskBoardItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        boolean flag = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        if (!level.isClientSide
                && !flag
                && player != null
                && level.getBlockEntity(pos) instanceof TaskBoardBlockEntity blockEntity
                && level.getBlockState(pos).getBlock() instanceof TaskBoardBlock block) {
            ((ServerPlayer) player).openMenu(new SimpleMenuProvider(blockEntity, blockEntity.getDisplayName()), pos);
        }

        return flag;
    }
}
