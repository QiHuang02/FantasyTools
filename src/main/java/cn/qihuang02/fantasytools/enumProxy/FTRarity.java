package cn.qihuang02.fantasytools.enumProxy;

import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

import java.util.function.UnaryOperator;

public class FTRarity {
    public static final EnumProxy<Rarity> LEGENDARY_RARITY_ENUM_PROXY =
            new EnumProxy<>(
                    Rarity.class,
                    -1,
                    "fantasytools:legendary",
                    (UnaryOperator<Style>) style -> style.withColor(16755200).withBold(true)
            );
}
