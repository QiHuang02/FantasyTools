package cn.qihuang02.fantasytools.recipe.custom;

import cn.qihuang02.fantasytools.recipe.FTRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record PortalTransformRecipe(
        Ingredient inputIngredient,
        Optional<ResourceKey<Level>> requiredCurrentDimension,
        Optional<ResourceKey<Level>> requiredTargetDimension,
        ItemStack resultTemplate,
        List<PortalTransformRecipe.ByproductDefinition> byproducts
) implements Recipe<SimpleItemInput> {
    private static final int MAX_BYPRODUCT_TYPES = 9;

    @Override
    public boolean matches(SimpleItemInput input, Level level) {
        return inputIngredient.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(SimpleItemInput input, HolderLookup.Provider registries) {
        ItemStack result = resultTemplate.copy();
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return resultTemplate.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(this.inputIngredient);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FTRecipes.PORTAL_TRANSFORM_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return FTRecipes.PORTAL_TRANSFORM_TYPE.get();
    }

    public Ingredient getInputIngredient() {
        return inputIngredient;
    }

    public List<ByproductDefinition> getByproducts() {
        return byproducts;
    }

    public Optional<ResourceKey<Level>> getRequiredCurrentDimension() {
        return requiredCurrentDimension;
    }

    public Optional<ResourceKey<Level>> getRequiredTargetDimension() {
        return requiredTargetDimension;
    }

    public record ByproductDefinition(
            ItemStack template,
            float chance,
            int minCount,
            int maxCount
    ) {
        public static final MapCodec<ByproductDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemStack.STRICT_CODEC.fieldOf("template").forGetter(ByproductDefinition::template),
                Codec.FLOAT.fieldOf("chance").forGetter(ByproductDefinition::chance),
                Codec.INT.fieldOf("min_count").forGetter(ByproductDefinition::minCount),
                Codec.INT.fieldOf("max_count").forGetter(ByproductDefinition::maxCount)
        ).apply(instance, ByproductDefinition::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ByproductDefinition> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC, ByproductDefinition::template,
                ByteBufCodecs.FLOAT, ByproductDefinition::chance,
                ByteBufCodecs.VAR_INT, ByproductDefinition::minCount,
                ByteBufCodecs.VAR_INT, ByproductDefinition::maxCount,
                ByproductDefinition::new
        );
    }

    /**
     * 验证配方数据的有效性 (用于 Codec)。
     *
     * @param recipe 待验证的配方实例
     * @return 如果有效，返回包含配方的 DataResult.success；否则返回 DataResult.error。
     */
    private static DataResult<PortalTransformRecipe> validate(PortalTransformRecipe recipe) {
        if (recipe.resultTemplate.isEmpty()) {
            return DataResult.error(() -> "Recipe result template cannot be empty");
        }

        List<ByproductDefinition> byproducts = recipe.byproducts();
        if (byproducts.size() > MAX_BYPRODUCT_TYPES) {
            return DataResult.error(() -> "Recipe cannot have more than " + MAX_BYPRODUCT_TYPES + " byproduct types, found " + byproducts.size());
        }

        for (int i = 0; i < byproducts.size(); i++) {
            if (byproducts.get(i).template.isEmpty()) {
                int finalI = i;
                return DataResult.error(() -> "Byproduct template at index " + finalI + " cannot be empty");
            }

            if (byproducts.get(i).minCount() <= 0 || byproducts.get(i).maxCount() < byproducts.get(i).minCount()) {
                int finalI1 = i;
                return DataResult.error(() -> "Byproduct at index " + finalI1 + " has invalid min/max counts");
            }
            if (byproducts.get(i).chance() <= 0 || byproducts.get(i).chance() > 1) {
                int finalI2 = i;
                return DataResult.error(() -> "Byproduct at index " + finalI2 + " has invalid chance (must be > 0 and <= 1)");
            }
        }
        return DataResult.success(recipe);
    }

    public static class Serializer implements RecipeSerializer<PortalTransformRecipe> {
        private static final MapCodec<PortalTransformRecipe> BASE_CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(PortalTransformRecipe::inputIngredient),
                        ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("required_current_dimension").forGetter(PortalTransformRecipe::requiredCurrentDimension),
                        ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("required_target_dimension").forGetter(PortalTransformRecipe::requiredTargetDimension),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(PortalTransformRecipe::resultTemplate),
                        ByproductDefinition.CODEC.codec().listOf().fieldOf("byproducts").orElse(Collections.emptyList()).forGetter(PortalTransformRecipe::byproducts)
                ).apply(instance, PortalTransformRecipe::new)
        );

        public static final MapCodec<PortalTransformRecipe> CODEC = BASE_CODEC
                .flatXmap(PortalTransformRecipe::validate,
                        PortalTransformRecipe::validate
                );

        public static final StreamCodec<RegistryFriendlyByteBuf, PortalTransformRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, PortalTransformRecipe::inputIngredient,
                ByteBufCodecs.optional(ResourceKey.streamCodec(Registries.DIMENSION)), PortalTransformRecipe::requiredCurrentDimension,
                ByteBufCodecs.optional(ResourceKey.streamCodec(Registries.DIMENSION)), PortalTransformRecipe::requiredTargetDimension,
                ItemStack.STREAM_CODEC, PortalTransformRecipe::resultTemplate,
                ByteBufCodecs.collection(ArrayList::new, PortalTransformRecipe.ByproductDefinition.STREAM_CODEC), PortalTransformRecipe::byproducts,
                PortalTransformRecipe::new
        );

        public MapCodec<PortalTransformRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, PortalTransformRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
