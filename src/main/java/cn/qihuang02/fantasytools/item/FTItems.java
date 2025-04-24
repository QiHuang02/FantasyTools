package cn.qihuang02.fantasytools.item;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.custom.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FTItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(FantasyTools.MODID);

    public static final DeferredItem<Item> ZHONGYA_HOURGLASS =
            ITEMS.register("zhongya_hourglass", () -> new Hourglass(
                    new Item.Properties())
            );
    public static final DeferredItem<Item> INVIS_CLOAK =
            ITEMS.register("invisibility_cloak", () -> new InvisCloak(
                    new Item.Properties()
            ));
    public static final DeferredItem<Item> DEMIGUISE_FUR =
            ITEMS.register("demiguise_fur", () -> new Item(
                    new Item.Properties()
                            .stacksTo(64)
                            .rarity(Rarity.valueOf("FANTASYTOOLS_LEGENDARY"))
            ));
    public static final DeferredItem<Item> BAMBOO_COPTER =
            ITEMS.register("bamboo_copter", () -> new BambooCopter(
                    new Item.Properties()
            ));
    public static final DeferredItem<Item> FULL_METAL =
            ITEMS.register("full_metal", () -> new FullMetal(
                    new Item.Properties()
            ));
    public static final DeferredItem<Item> FOUR_DIMENSIONAL_POCKET =
            ITEMS.register("four_dimensional_pocket", () -> new FourDimensionalPocket(
                    new Item.Properties()
            ));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
