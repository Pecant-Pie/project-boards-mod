package com.pecantpie;

import com.pecantpie.block.TaskBoardBlockEntity;
import com.pecantpie.screen.TaskBoardMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ProjectBoardData {

    public record EditTaskData(String name) implements CustomPacketPayload {

        public static final CustomPacketPayload.Type<EditTaskData> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ProjectBoards.MODID, "task_data"));

        // Each pair of elements defines the stream codec of the element to encode/decode and the getter for the element to encode
        // 'name' will be encoded and decoded as a string
        // 'age' will be encoded and decoded as an integer
        // The final parameter takes in the previous parameters in the order they are provided to construct the payload object
        public static final StreamCodec<ByteBuf, EditTaskData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                EditTaskData::name,
                EditTaskData::new
        );

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static class ServerPayloadHandler {
            public static void handleDataOnMain(final EditTaskData data, final IPayloadContext context) {
                // Do something with the data, on the main thread
                AbstractContainerMenu menu = context.player().containerMenu;

                if (menu instanceof TaskBoardMenu tbbm) {
                    TaskBoardBlockEntity tbbe = ((TaskBoardMenu) menu).blockEntity;
                    tbbe.setTaskName(data.name());
                    tbbe.initTaskStatus(); // only resets status if it had none
                }
            }
        }

    }

    public enum TaskStatus {
        PLANNED,
        STARTED,
        COMPLETED,
        BLOCKED;

        public TaskStatus next() {
            return switch (this) {
                case PLANNED -> STARTED;
                case STARTED, COMPLETED -> COMPLETED;
                case BLOCKED -> BLOCKED;
            };
        }

        public TaskStatus previous() {
            return switch (this) {
                case COMPLETED -> STARTED;
                case PLANNED, STARTED -> PLANNED;
                case BLOCKED -> BLOCKED;
            };
        }

        public static TaskStatus fromShort(short statusOrdinal) {
            return switch (statusOrdinal) {
//                case 0 -> PLANNED; // included in default
                case 1 -> STARTED;
                case 2 -> COMPLETED;
                case 3 -> BLOCKED;
                default -> PLANNED;
            };
        }

        public static short toShort(TaskStatus statusEnum) {
            return switch (statusEnum)  {
//                case PLANNED -> 0; // included in default
                case STARTED -> 1;
                case COMPLETED -> 2;
                case BLOCKED -> 3;
                default -> 0;
            };
        }


        public Component getTranslatableComponent() {
            return Component.translatable("item.projectboards.task_slip." + this.toString().toLowerCase());
        }
    }




}
