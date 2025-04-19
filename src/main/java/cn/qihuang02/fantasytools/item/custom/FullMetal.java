package cn.qihuang02.fantasytools.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class FullMetal extends Item {
    public FullMetal(Properties properties) {
        super(properties.stacksTo(99).rarity(Rarity.valueOf("FANTASYTOOLS_UNIQUE")));
    }
}
