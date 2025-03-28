package cn.qihuang02.fantasytools.item;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.custom.INV_CLOAK;
import cn.qihuang02.fantasytools.item.custom.Zhongya;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FTItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(FantasyTools.MODID);

    public static final DeferredItem<Item> DEMIGUISE_FUR =
            ITEMS.register("demiguise_fur", () -> new Item(
                    new Item.Properties()
                            .rarity(Rarity.valueOf("FANTASYTOOLS_LEGENDARY"))
                            .stacksTo(64)
            ));
    public static final DeferredItem<Item> ZHONGYAHOURGLASS =
            ITEMS.register("zhongya_hourglass", () -> new Zhongya(new Item.Properties()));
    public static final DeferredItem<Item> INV_CLOAK =
            ITEMS.register("invisibility_cloak", () -> new INV_CLOAK(new Item.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
