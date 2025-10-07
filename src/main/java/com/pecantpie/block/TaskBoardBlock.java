package com.pecantpie.block;

import com.mojang.serialization.MapCodec;
import com.pecantpie.Config;
import com.pecantpie.ProjectBoards;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class TaskBoardBlock extends BaseEntityBlock {

    public static final MapCodec<TaskBoardBlock> CODEC = simpleCodec(TaskBoardBlock::new);
    public static final DirectionProperty FACING;

    public TaskBoardBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(((this.stateDefinition.any()).setValue(FACING, Direction.NORTH)));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TaskBoardBlockEntity tbbe) {
            ItemStack stack = new ItemStack(ProjectBoards.TASK_SLIP.get());
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(Config.defaultTaskName));
            tbbe.inventory.insertItem(0, stack, false);
        }
    }

    /* BLOCKSTATE STUFF */

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    @NotNull
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    @NotNull
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.mirror(mirror);
    }

    /* BLOCKENTITY STUFF */

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TaskBoardBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return super.getTicker(level, state, blockEntityType);
    }

    @Override
    public @Nullable <T extends BlockEntity> GameEventListener getListener(ServerLevel level, T blockEntity) {
        return super.getListener(level, blockEntity);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(ProjectBoards.TASK_BOARD)) {
            if (level.getBlockEntity(pos) instanceof TaskBoardBlockEntity) {
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(ProjectBoards.TASK_SLIP) && level.getBlockEntity(pos) instanceof TaskBoardBlockEntity tbbe) {

            // check if the task board already has an item
            if (!tbbe.inventory.getStackInSlot(0).isEmpty()) {
                ItemStack taskFromBoard = tbbe.inventory.getStackInSlot(0);
                // If the previous task has a different name from the default,
                // try to return it to the player before replacing it.
                if (!Objects.equals(taskFromBoard.get(DataComponents.CUSTOM_NAME), Component.literal(Config.defaultTaskName))) {

                    // If the player can't pick up the task slip, don't do anything!
                    if (stack.getCount() == 1) {
                        ItemStack taskFromPlayer = stack.split(1);
                        player.setItemInHand(hand, taskFromBoard);
                        tbbe.inventory.setStackInSlot(0, taskFromPlayer);
                        level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
                    }
                } else {
                    ItemStack taskFromPlayer = stack.split(1);
                    tbbe.inventory.setStackInSlot(0, taskFromPlayer);
                    level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
                }
            } else {
                ItemStack taskFromPlayer = stack.split(1);
                tbbe.inventory.setStackInSlot(0, taskFromPlayer);
                level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
            }
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    static {
        FACING = DirectionalBlock.FACING;
    }


}
