package cn.qihuang02.fantasytools.enumproxy;

import net.minecraft.ChatFormatting;
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
                    (UnaryOperator<Style>) style -> style.withColor(ChatFormatting.GOLD).withBold(true)
            );

    public static final EnumProxy<Rarity> UNIQUE_RARITY_ENUM_PROXY =
            new EnumProxy<>(
                    Rarity.class,
                    -2,
                    "fantasytools:unique",
                    (UnaryOperator<Style>) style -> style.withColor(ChatFormatting.RED).withBold(true)
            );
}
