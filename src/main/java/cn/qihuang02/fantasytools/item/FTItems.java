package cn.qihuang02.fantasytools.item;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.custom.Zhongya;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FTItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(FantasyTools.MODID);

    public static final DeferredItem<Item> ZHONGYAHOURGLASS =
            ITEMS.register("zhongya_hourglass", () -> new Zhongya(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
