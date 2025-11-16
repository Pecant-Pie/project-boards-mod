package com.pecantpie.screen;

import com.pecantpie.ProjectBoards;
import com.pecantpie.block.TaskBoardBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;


public class TaskBoardMenu extends AbstractContainerMenu {
    public final TaskBoardBlockEntity blockEntity;
    public final Level level;

    public TaskBoardMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public TaskBoardMenu(int containerId, Inventory inv, BlockEntity blockEntity) {
        super(ProjectBoards.TASK_BOARD_MENU.get(), containerId);
        if (blockEntity instanceof TaskBoardBlockEntity tbbe) {
            this.blockEntity = tbbe;
            tbbe.markInUse();
            this.level = inv.player.level();
        } else {
            throw new UnsupportedOperationException(inv.player.getName().getString() + " tried to open a TaskBoardMenu without a TaskBoardBlockEntity!");
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ProjectBoards.TASK_BOARD.get());
    }


    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }


}
