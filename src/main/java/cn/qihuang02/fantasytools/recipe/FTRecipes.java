package cn.qihuang02.fantasytools.recipe;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.recipe.custom.PortalTransformRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FTRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, FantasyTools.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, FantasyTools.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<PortalTransformRecipe>> PORTAL_TRANSFORM_SERIALIZER =
            RECIPE_SERIALIZERS.register("portal_transform", PortalTransformRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<PortalTransformRecipe>> PORTAL_TRANSFORM_TYPE =
            RECIPE_TYPES.register("portal_transform", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return "portal_transform";
                }
            });

    public static void register(IEventBus eventBus) {
        RECIPE_SERIALIZERS.register(eventBus);
        RECIPE_TYPES.register(eventBus);
        FantasyTools.LOGGER.info("Registering {}:portal_transform", FantasyTools.MODID);
    }
}
