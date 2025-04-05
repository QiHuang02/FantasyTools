package cn.qihuang02.fantasytools.compat;

import cn.qihuang02.fantasytools.item.FTItems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;

//@EventBusSubscriber(modid = FantasyTools.MODID, bus = EventBusSubscriber.Bus.MOD)
public class CuriosCompat {

    @SubscribeEvent
    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerItem(
                CuriosCapability.ITEM,
                (stack, context) -> new ZhongyaCurio(stack),
                FTItems.ZHONGYAHOURGLASS.get()
        );
    }

    static class ZhongyaCurio implements ICurio {
        private final ItemStack stack;

        public ZhongyaCurio(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public ItemStack getStack() {
            return stack;
        }
    }
}
