package cn.qihuang02.fantasytools.compat.kubejs;

import cn.qihuang02.fantasytools.compat.kubejs.components.LevelComponent;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.MapRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.NumberComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.Map;

public interface PortalTransformRecipeSchema {
    RecipeKey<Ingredient> INPUT = IngredientComponent.INGREDIENT.inputKey("input");
    RecipeKey<ItemStack> RESULT = ItemStackComponent.ITEM_STACK.outputKey("result");

    RecipeKey<ResourceKey<Level>> CURRENT_DIMENSION = LevelComponent.DIMENSION.otherKey("current_dimension").defaultOptional();
    RecipeKey<ResourceKey<Level>> TARGET_DIMENSION = LevelComponent.DIMENSION.otherKey("target_dimension").defaultOptional();

//    RecipeKey<ItemStack> BYPRODUCT_ITEM = ItemStackComponent.ITEM_STACK.outputKey("byproduct");
//    RecipeKey<Double> BYPRODUCT_CHANCE = NumberComponent.DOUBLE.range(0.1, 1.0).otherKey("chance").optional(0.1);
//    RecipeKey<Integer> BYPRODUCT_MIN = NumberComponent.INT.min(1).otherKey("min_chance").optional(1);
//    RecipeKey<Integer> BYPRODUCT_MAX = NumberComponent.INT.min(1).otherKey("max_chance").optional(1);



    RecipeSchema PORTAL_TRANSFORM = new RecipeSchema(INPUT, RESULT, CURRENT_DIMENSION, TARGET_DIMENSION);
}
