package cn.qihuang02.fantasytools.compat.kubejs;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public interface PortalTransformRecipeSchema {
    RecipeKey<Ingredient> INPUT = IngredientComponent.INGREDIENT.inputKey("ingredient");
    RecipeKey<ItemStack> RESULT = ItemStackComponent.ITEM_STACK.outputKey("result");

    RecipeSchema PORTAL_TRANSFORM = new RecipeSchema(INPUT, RESULT);
}
