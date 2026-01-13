package com.pecantpie.block;

import com.pecantpie.Config;
import com.pecantpie.ProjectBoardData;
import com.pecantpie.ProjectBoards;
import com.pecantpie.component.ModDataComponents;
import com.pecantpie.item.TaskSlipItem;
import com.pecantpie.screen.TaskBoardMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

import static com.pecantpie.Config.defaultTaskName;
import static com.pecantpie.Config.taskNameMaxLength;
import static com.pecantpie.ProjectBoards.MODID;
import static com.pecantpie.ProjectBoardData.TaskStatus;
import static com.pecantpie.item.TaskSlipItem.getRawStatus;
import static com.pecantpie.item.TaskSlipItem.resetTaskStatus;

public class TaskBoardBlockEntity extends BlockEntity implements MenuProvider{
    private static final int TASK_SLOT = 0;
    private UUID usingPlayer = null;

    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
            super.onContentsChanged(slot);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return super.isItemValid(slot, stack) && stack.is(ProjectBoards.TASK_SLIP);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {

            if (canAutomationUse()) {
                return super.insertItem(slot, stack, simulate);
            } else {
                return stack;
            }
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {

            if (canAutomationUse()) {
                return super.extractItem(slot, amount, simulate);
            } else {
                return ItemStack.EMPTY;
            }
        }
    };

    public TaskBoardBlockEntity( BlockPos pos, BlockState blockState) {
        super(ProjectBoards.TASK_BOARD_BLOCK_ENTITY.get(), pos, blockState);
    }


    public ItemStack getTaskItem() {
        return inventory.getStackInSlot(TASK_SLOT);
    }

    public Component getTaskName() {
        // check if task board has a task
        ItemStack item = getTaskItem();
        if (!item.isEmpty()) {
            Component name = item.get(DataComponents.CUSTOM_NAME);
            name = name != null ? name : Component.empty();
            // check if task has a non-default name.
            if (!Objects.equals(name, Component.literal(Config.defaultTaskName))) {
                return Component.literal(name.getString());
            }
        }
        return Component.empty();
    }

    public String getTaskString() {
        return getTaskName().getString();
    }


    /// returns true if there is a task slip inside and the name is valid, false otherwise
    public boolean setTaskName(String name) {
        // check if task board has a task AND the name fits!
        if (this.hasTask() && this.isTaskNameValid(name)) {
            this.forceSetTaskName(name);
            markUpdated();
            return true;
        } else {
            return false;
        }
    }



    /// returns true if there is NO task slip inside and the name is valid, false otherwise
    public boolean createNewTask(String name, Player player) {
        if (!this.hasTask() && this.isTaskNameValid(name) && canUse(player)) {
            forceCreateNewTask();
            forceSetTaskName(name);
            markUpdated();
            return true;
        } else {
            return false;
        }
    }

    /// returns true if there is NO task slip inside and the name is valid, false otherwise
    public boolean createNewTask(String name) {
        if (!this.hasTask() && this.isTaskNameValid(name) && canAutomationUse()) {
            forceCreateNewTask();
            forceSetTaskName(name);
            markUpdated();
            return true;
        } else {
            return false;
        }
    }

    /// returns true if there is NO task slip inside, false otherwise
    /// creates a task with the default name, from the Mod's config
    public boolean createNewTask(@Nullable Player player) {
        return createNewTask(defaultTaskName, player);
    }


    public boolean hasTask() {
        return !getTaskItem().isEmpty();
    }

    public boolean isTaskNameValid(String name) {
        return name.length() < taskNameMaxLength;
    }

    public void resetTaskOwner() {
        ItemStack task = getTaskItem();
        if (!task.isEmpty()) {
            task.remove(ModDataComponents.OWNER_NAME);
            task.remove(ModDataComponents.OWNER_UUID);
        }
        markUpdated();
    }

    public void setTaskOwner(Player player) {
        ItemStack task = getTaskItem();
        if (!task.isEmpty()) {
            task.set(ModDataComponents.OWNER_NAME, player.getName().tryCollapseToString());
            task.set(ModDataComponents.OWNER_UUID, player.getStringUUID());
        }
        markUpdated();
    }

    @NotNull
    public String getTaskOwnerName() {
        ItemStack task = getTaskItem();
        if (!task.isEmpty()) {
            String name = task.get(ModDataComponents.OWNER_NAME);
            return Objects.requireNonNullElse(name, "");
        }
        else {
            return "";
        }
    }

    public void initTaskStatus() {
        ItemStack item = getTaskItem();
        if (!item.isEmpty() && !item.has(ModDataComponents.STATUS_CODE)) {
            TaskSlipItem.resetTaskStatus(item);
        }
        markUpdated();
    }

    public void incrementTaskStatus() {
        TaskSlipItem.incrementTaskStatus(getTaskItem());
        markUpdated();
    }

    public void decrementTaskStatus() {
        TaskSlipItem.decrementTaskStatus(getTaskItem());
        markUpdated();
    }

    public Component getTaskStatusComponent() {
        return TaskSlipItem.getTaskStatusComponent(getTaskItem());
    }

    public IItemHandler getItemHandler() {
        return inventory;
    }


    public ItemStack removeTask() {
        ItemStack item = getTaskItem();
        inventory.setStackInSlot(TASK_SLOT, ItemStack.EMPTY);
        markUpdated();
        return item;
    }

    public boolean canUse(@Nullable Player player) {
        return usingPlayer == null || (player != null && player.getUUID().equals(usingPlayer));
    }

    public boolean canAutomationUse() {
        return usingPlayer == null;
    }

    public void markInUse(Player player) {
        usingPlayer = player.getUUID();
    }

    public void markNotInUse() {
        usingPlayer = null;
        this.level.updateNeighborsAt(this.getBlockPos(), this.getBlockState().getBlock());
    }

    private void markUpdated() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

// ******************
// ** ABSTRACTIONS **
// ******************

    protected void forceSetTaskName(String name) {
        ItemStack item = getTaskItem();
        item.set(DataComponents.CUSTOM_NAME, Component.literal(name));
    }


    protected void forceCreateNewTask() {
        ItemStack newTask = new ItemStack(ProjectBoards.TASK_SLIP.get());
        resetTaskStatus(newTask);
        inventory.setStackInSlot(TASK_SLOT, newTask);
        resetTaskOwner();
    }

// ***************
// ** OVERRIDES **
// ***************

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.projectboards.edit_task_screen");
    }


    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new TaskBoardMenu(i, inventory, this);
    }


    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }



}
