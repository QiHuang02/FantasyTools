package cn.qihuang02.fantasytools.recipe.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record Byproducts(
        ItemStack byproduct,
        float chance,
        int minCount,
        int maxCount
) {
    public static final MapCodec<Byproducts> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.STRICT_CODEC.fieldOf("byproduct").forGetter(Byproducts::byproduct),
            Codec.FLOAT.fieldOf("chance").forGetter(Byproducts::chance),
            Codec.INT.fieldOf("min_count").forGetter(Byproducts::minCount),
            Codec.INT.fieldOf("max_count").forGetter(Byproducts::maxCount)
    ).apply(instance, Byproducts::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Byproducts> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, Byproducts::byproduct,
            ByteBufCodecs.FLOAT, Byproducts::chance,
            ByteBufCodecs.INT, Byproducts::minCount,
            ByteBufCodecs.INT, Byproducts::maxCount,
            Byproducts::new
    );

    public ItemStack byproduct() {
        return byproduct;
    }

    public float chance() {
        return chance;
    }

    public int minCount() {
        return minCount;
    }

    public int maxCount() {
        return maxCount;
    }
}