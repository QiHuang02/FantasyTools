package cn.qihuang02.fantasytools.compat.kubejs.recipe;

import cn.qihuang02.fantasytools.FantasyTools;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;

public class PortalTransformKubeRecipe extends KubeRecipe {
    public static final KubeRecipeFactory FACTORY = new KubeRecipeFactory(
            FantasyTools.getRL("portal_transform"),
            PortalTransformKubeRecipe.class,
            PortalTransformKubeRecipe::new
    );
}
