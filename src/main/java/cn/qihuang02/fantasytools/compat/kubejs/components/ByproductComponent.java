package cn.qihuang02.fantasytools.compat.kubejs.components;

import cn.qihuang02.fantasytools.recipe.custom.Byproducts;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.util.List;
import java.util.Optional;

public class ByproductComponent implements RecipeComponent<Optional<List<Byproducts>>> {
    public static final ByproductComponent BYPRODUCT = new ByproductComponent();

    private ByproductComponent() {
        super();
    }

    @Override
    public Codec<Optional<List<Byproducts>>> codec() {
        return null;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(Optional.class);
    }

    @Override
    public String toString() {
        return "byproduct";
    }
}