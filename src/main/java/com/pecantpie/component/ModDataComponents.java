package com.pecantpie.component;

import com.pecantpie.ProjectBoards;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ProjectBoards.MODID);


    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> OWNER_UUID = register("owner_uuid", builder -> builder.persistent(ExtraCodecs.NON_EMPTY_STRING));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> OWNER_NAME = register("owner_name", builder -> builder.persistent(ExtraCodecs.PLAYER_NAME));


    private static <T>DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
