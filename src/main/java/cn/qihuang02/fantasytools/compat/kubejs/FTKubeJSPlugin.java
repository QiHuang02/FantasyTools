package cn.qihuang02.fantasytools.compat.kubejs;

import cn.qihuang02.fantasytools.compat.kubejs.binding.ByproductsBinding;
import cn.qihuang02.fantasytools.compat.kubejs.components.ByproductsComponent;
import cn.qihuang02.fantasytools.compat.kubejs.components.LevelComponent;
import cn.qihuang02.fantasytools.compat.kubejs.recipe.PortalTransformKubeRecipe;
import cn.qihuang02.fantasytools.compat.kubejs.schema.PortalTransformRecipeSchema;
import cn.qihuang02.fantasytools.recipe.FTRecipes;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeComponentFactoryRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeFactoryRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import dev.latvian.mods.kubejs.script.BindingRegistry;

public class FTKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void registerBindings(BindingRegistry bindings) {
        if (bindings.type().isServer()) {
            bindings.add("Byproduct", ByproductsBinding.class);
        }
    }

    @Override
    public void registerRecipeComponents(RecipeComponentFactoryRegistry registry) {
        registry.register(LevelComponent.DIMENSION);
        registry.register(ByproductsComponent.BYPRODUCT);
    }

    @Override
    public void registerRecipeFactories(RecipeFactoryRegistry registry) {
        registry.register(PortalTransformKubeRecipe.FACTORY);
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        registry.register(FTRecipes.PORTAL_TRANSFORM_TYPE.getId(), PortalTransformRecipeSchema.PORTAL_TRANSFORM);
    }
}
