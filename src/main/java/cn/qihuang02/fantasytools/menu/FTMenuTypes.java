package cn.qihuang02.fantasytools.menu;

import cn.qihuang02.fantasytools.FantasyTools;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FTMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, FantasyTools.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<PocketMenu>> POCKET_MENU_TYPE =
            MENU_TYPES.register("pocket_menu",
                    () -> IMenuTypeExtension.create(PocketMenu::createClientSide)
            );

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
