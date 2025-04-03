package cn.qihuang02.fantasytools.item.custom;

import cn.qihuang02.fantasytools.item.FTArmorMaterials;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Invis_cloak extends ArmorItem {
    public Invis_cloak(@NotNull Properties properties) {
        super(
                FTArmorMaterials.INVCLOAK_ARMOR_MATERIAL,
                Type.CHESTPLATE,
                properties
                        .stacksTo(1)
                        .rarity(Rarity.valueOf("FANTASYTOOLS_LEGENDARY"))
        );
    }

    @Override
    public void appendHoverText(
            @NotNull ItemStack stack,
            @NotNull TooltipContext context,
            @NotNull List<Component> tooltipComponents,
            @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
