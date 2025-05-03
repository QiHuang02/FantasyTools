package cn.qihuang02.fantasytools.compat.emi;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.recipe.FTRecipes;
import cn.qihuang02.fantasytools.recipe.custom.PortalTransformRecipe;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

@EmiEntrypoint
public class FTEmiClientPlugin implements EmiPlugin {
    public static final ResourceLocation CATEGORY_ID = FantasyTools.getRL("portal_transform");
    public static final EmiRecipeCategory PORTAL_TRANSFORM_CATEGORY = new EmiRecipeCategory(
            CATEGORY_ID,
            EmiStack.of(Items.ENDER_PEARL)
    );

    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(PORTAL_TRANSFORM_CATEGORY);

        RecipeManager recipeManager = null;
        if (Minecraft.getInstance().level != null) {
            recipeManager = Minecraft.getInstance().level.getRecipeManager();
        }
        if (recipeManager != null) {
            for (RecipeHolder<PortalTransformRecipe> holder : recipeManager.getAllRecipesFor(FTRecipes.PORTAL_TRANSFORM_TYPE.get())) {
                registry.addRecipe(new FTEmiRecipe(holder));
            }
        }

        FantasyTools.LOGGER.info("Registering portal_transform recipes with EMI");
    }
}
