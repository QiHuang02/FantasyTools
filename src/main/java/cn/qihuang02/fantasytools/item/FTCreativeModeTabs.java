package cn.qihuang02.fantasytools.item;

import cn.qihuang02.fantasytools.FantasyTools;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FTCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FantasyTools.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FANTANSYTOOLS_TAB =
            CREATIVE_MODE_TABS.register("fantasytools_tab",
                    () -> CreativeModeTab
                            .builder()
                            .title(Component.translatable("item.fantasytools.tab"))
                            .icon(() -> FTItems.ZHONGYAHOURGLASS.get().getDefaultInstance())
                            .displayItems(((parameters, output) ->{
                                output.accept(FTItems.ZHONGYAHOURGLASS.get());
                                output.accept(FTItems.INVIS_CLOAK.get());
                                output.accept(FTItems.DEMIGUISE_FUR.get());
                            }))
                            .build()
            );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
