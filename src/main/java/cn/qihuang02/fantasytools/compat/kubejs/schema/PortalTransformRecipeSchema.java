package cn.qihuang02.fantasytools.compat.kubejs.schema;

import cn.qihuang02.fantasytools.compat.kubejs.components.ByproductsComponent;
import cn.qihuang02.fantasytools.compat.kubejs.components.LevelComponent;
import cn.qihuang02.fantasytools.compat.kubejs.recipe.PortalTransformKubeRecipe;
import cn.qihuang02.fantasytools.recipe.custom.Byproducts;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.List;

public interface PortalTransformRecipeSchema {
    RecipeKey<Ingredient> INPUT = IngredientComponent.INGREDIENT.inputKey("input");
    RecipeKey<ItemStack> RESULT = ItemStackComponent.ITEM_STACK.outputKey("result");

    RecipeKey<ResourceKey<Level>> CURRENT_DIMENSION = LevelComponent.DIMENSION.otherKey("current_dimension").defaultOptional();
    RecipeKey<ResourceKey<Level>> TARGET_DIMENSION = LevelComponent.DIMENSION.otherKey("target_dimension").defaultOptional();

    RecipeKey<List<Byproducts>> BYPRODUCTS = ByproductsComponent.LIST.otherKey("byproducts").defaultOptional();

    @Info(params = {
            @Param(name = "Input", value = "Input Ingredient"),
            @Param(name = "Output", value = "Output ItemStack"),
            @Param(name = "current_dimension", value = "specifies the activation dimension where a recipe's input must be processed."),
            @Param(name = "target_dimension", value = "defines the destination plane for portal-based transformations in cross-dimensional recipes."),
            @Param(name = "byproducts", value = "Byproducts")
    })
    RecipeSchema PORTAL_TRANSFORM = new RecipeSchema(
            INPUT,
            RESULT,
            CURRENT_DIMENSION,
            TARGET_DIMENSION,
            BYPRODUCTS
    ).factory(PortalTransformKubeRecipe.FACTORY);
}
