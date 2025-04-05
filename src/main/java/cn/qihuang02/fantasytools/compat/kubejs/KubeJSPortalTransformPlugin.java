package cn.qihuang02.fantasytools.compat.kubejs;

import cn.qihuang02.fantasytools.recipe.FTRecipes;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RecipeComponentFactoryRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;

public class KubeJSPortalTransformPlugin implements KubeJSPlugin {

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        registry.register(FTRecipes.PORTAL_TRANSFORM_TYPE.getId(), PortalTransformRecipeSchema.PORTAL_TRANSFORM);
    }

    @Override
    public void registerRecipeComponents(RecipeComponentFactoryRegistry registry) {
        KubeJSPlugin.super.registerRecipeComponents(registry);
    }
}
