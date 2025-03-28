package cn.qihuang02.fantasytools.item.custom;

import cn.qihuang02.fantasytools.item.FTArmorMaterials;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Rarity;

public class INV_CLOAK extends ArmorItem {
    public INV_CLOAK(Properties properties) {
        super(
                FTArmorMaterials.INVCLOAK_ARMOR_MATERIAL,
                Type.CHESTPLATE,
                properties
                        .stacksTo(1)
                        .rarity(Rarity.valueOf("FANTASYTOOLS_LEGENDARY"))
                        .durability(-1)
        );
    }


}
