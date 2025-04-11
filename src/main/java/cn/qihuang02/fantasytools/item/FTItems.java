package cn.qihuang02.fantasytools.item;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.custom.Hourglass;
import cn.qihuang02.fantasytools.item.custom.Invis_cloak;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FTItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(FantasyTools.MODID);

    public static final DeferredItem<Item> ZHONGYAHOURGLASS =
            ITEMS.register("zhongya_hourglass", () -> new Hourglass(
                    new Item.Properties())
            );
    public static final DeferredItem<Item> INVIS_CLOAK =
            ITEMS.register("invisibility_cloak", () -> new Invis_cloak(
                    new Item.Properties()
            ));
    public static final DeferredItem<Item> DEMIGUISE_FUR =
            ITEMS.register("demiguise_fur", () -> new Item(
                    new Item.Properties()
                            .rarity(Rarity.valueOf("FANTASYTOOLS_LEGENDARY"))
                            .stacksTo(64)
            ));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
