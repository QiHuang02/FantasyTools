package cn.qihuang02.fantasytools.compat.kubejs.components;

import cn.qihuang02.fantasytools.recipe.custom.PortalTransformRecipe;
import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.util.List;
import java.util.Optional;

public class ByproductComponent implements RecipeComponent<Optional<List<PortalTransformRecipe.ByproductDefinition>>> {
    public static final ByproductComponent BYPRODUCT = new ByproductComponent();

    private ByproductComponent() {
        super();
    }

    @Override
    public Codec<Optional<List<PortalTransformRecipe.ByproductDefinition>>> codec() {
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
