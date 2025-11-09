package com.pecantpie.block;

import com.pecantpie.Config;
import com.pecantpie.ProjectBoards;
import com.pecantpie.component.ModDataComponents;
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
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.pecantpie.Config.defaultTaskName;
import static com.pecantpie.Config.taskNameMaxLength;
import static com.pecantpie.ProjectBoards.MODID;

public class TaskBoardBlockEntity extends BlockEntity implements MenuProvider {
    private static final int TASK_SLOT = 0;

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
    public boolean createNewTask(String name) {
        if (!this.hasTask() && this.isTaskNameValid(name)) {
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
    public boolean createNewTask() {
        return createNewTask(defaultTaskName);
    }


    public boolean hasTask() {
        return !getTaskItem().isEmpty();
    }

    public boolean isTaskNameValid(String name) {
        return name.length() < taskNameMaxLength;
    }

    public void setTaskOwner(Player player) {
        ItemStack task = getTaskItem();
        if (!task.isEmpty()) {
            task.set(ModDataComponents.OWNER_NAME, player.getName().tryCollapseToString());
            task.set(ModDataComponents.OWNER_UUID, player.getStringUUID());
        }
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
        inventory.setStackInSlot(TASK_SLOT, newTask);
    }

// ***************
// ** OVERRIDES **
// ***************

    @Override
    public Component getDisplayName() {
        return Component.translationArg(ResourceLocation.fromNamespaceAndPath(MODID, "gui/edit_task_screen"));
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
