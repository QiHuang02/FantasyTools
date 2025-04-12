package cn.qihuang02.fantasytools.component;

import cn.qihuang02.fantasytools.FantasyTools;
import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;
import java.util.function.UnaryOperator;

public class FTComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, FantasyTools.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> OWNER =
            register("owner", builder -> builder.persistent(UUIDUtil.CODEC));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> NO_PORTAL_TRANSFORM =
            register("no_portal_transform", builder -> builder.persistent(Codec.BOOL));

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(
            String name,
            UnaryOperator<DataComponentType.Builder<T>> builderOperator
    ) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
