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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
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
import net.minecraft.world.phys.Vec3;
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

//        // open task editing menu for the player
//        if (!level.isClientSide()) {
//            ((ServerPlayer) player).openMenu(new SimpleMenuProvider(tbbe, tbbe.getDisplayName()), pos);
//        }
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
        // if this block has a block entity associated with it
        if (level.getBlockEntity(pos) instanceof TaskBoardBlockEntity tbbe && !tbbe.isInUse()) {
            // if the player used a Task Slip item to interact
            if (stack.is(ProjectBoards.TASK_SLIP)) {

                // check if the task board already has an item
                if (!tbbe.inventory.getStackInSlot(0).isEmpty()) {
                    ItemStack taskFromBoard = tbbe.inventory.getStackInSlot(0);

                    // Check if the previous task has a different name from the default,
                    // so we can try to return it to the player before replacing it.
                    if (!Objects.equals(taskFromBoard.get(DataComponents.CUSTOM_NAME), Component.literal(Config.defaultTaskName))) {

                        // Only proceed if the player can pick up the task slip in the same hand!
                        if (stack.getCount() == 1) {
                            ItemStack taskFromPlayer = stack.split(1);
                            player.setItemInHand(hand, taskFromBoard);
                            tbbe.inventory.setStackInSlot(0, taskFromPlayer);
                            level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
                        }

                    // No need to give the player back an empty slip! Just take theirs and replace the empty one
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

            // Task board was opened with a non-task slip item

            // Also check if the click was on the owner section and there is a task inside
            } else if (isClickOnOwner(hitResult, state) && !tbbe.inventory.getStackInSlot(0).isEmpty()) {
                if (player.isCrouching()) {
                    tbbe.resetTaskOwner();
                    level.playSound(player, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1f, 1f);
                    return ItemInteractionResult.SUCCESS;
                } else {
                    tbbe.setTaskOwner(player);
                    level.playSound(player, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1f, 2f);
                    return ItemInteractionResult.SUCCESS;
                }

            // click was on the status increment part of the block
            } else if (isClickOnStatus(hitResult, state) && !tbbe.inventory.getStackInSlot(0).isEmpty()) {
                if (player.isCrouching()) {
                    tbbe.decrementTaskStatus();
                    level.playSound(player, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1f, 1f);
                    return ItemInteractionResult.SUCCESS;
                } else {
                    tbbe.incrementTaskStatus();
                    level.playSound(player, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1f, 2f);
                    return ItemInteractionResult.SUCCESS;
                }

            // click was on the task part of the block, so we should edit the current task or make a new one!
            } else {

                // check if: there is not a task slip item inside
                if (tbbe.inventory.getStackInSlot(0).isEmpty() && !player.isCrouching()) {
                    tbbe.createNewTask();
                // let player take out task if sneak-clicking with an empty hand
                } else if (player.isCrouching() && player.getMainHandItem().isEmpty()
                ) {
                    ItemStack taskFromBoard = tbbe.removeTask();
                    level.playSound(player, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1f, 1f);
                    if (!Objects.equals(taskFromBoard.get(DataComponents.CUSTOM_NAME), Component.literal(Config.defaultTaskName))) {
                        player.setItemInHand(hand, taskFromBoard);
                        return ItemInteractionResult.SUCCESS;
                    } else {
                        return ItemInteractionResult.FAIL;
                    }
                }


                if (!level.isClientSide()) {
                    ((ServerPlayer) player).openMenu(new SimpleMenuProvider(tbbe, tbbe.getDisplayName()), pos);
                }
                return ItemInteractionResult.SUCCESS;
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    private boolean isClickOnOwner(BlockHitResult hitResult, BlockState state) {
        BlockPos hitBlock = hitResult.getBlockPos();
        Vec3 hitLocation = hitResult.getLocation();
        return hitLocation.y() > (hitBlock.getY() + (13f / 16f))
                && hitResult.getDirection() == state.getValue(FACING);
    }

    private boolean isClickOnStatus(BlockHitResult hitResult, BlockState state) {
        BlockPos hitBlock = hitResult.getBlockPos();
        Vec3 hitLocation = hitResult.getLocation();

        return hitLocation.y() < (hitBlock.getY() + (3f / 16f))
                && hitResult.getDirection() == state.getValue(FACING);
    }

    private boolean isClickOnStatusDecrement(BlockHitResult hitResult, BlockState state, Player player) {
        BlockPos hitBlock = hitResult.getBlockPos();
        Vec3 hitLocation = hitResult.getLocation();

        boolean isLeftSide = player.isCrouching();

        return hitLocation.y() < (hitBlock.getY() + (3f / 16f)) && isLeftSide
                && hitResult.getDirection() == state.getValue(FACING);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    static {
        FACING = DirectionalBlock.FACING;
    }


}
