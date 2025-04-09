package cn.qihuang02.fantasytools.compat.kubejs.binding;

import cn.qihuang02.fantasytools.recipe.custom.Byproducts;
import cn.qihuang02.fantasytools.recipe.custom.CountRange;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ByproductsBinding {
    private ByproductsBinding() {
    }

    public static Byproducts of(ItemStack byproduct) {
        return create(byproduct, 1f, 1, 1);
    }

    public static Byproducts of(ItemStack byproduct, float chance) {
        return create(byproduct, chance, 1, 1);
    }

    public static Byproducts of(ItemStack byproduct, float chance, int min, int max) {
        return create(byproduct, chance, min, max);
    }

    private static Byproducts create(ItemStack item, float chance, int min, int max) {
        return new Byproducts(item, chance, new CountRange(min, max));
    }

    private static List<Byproducts> listOf(Byproducts... byproducts) {
        return List.of(byproducts);
    }
}
