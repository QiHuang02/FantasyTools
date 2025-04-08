package cn.qihuang02.fantasytools.compat.kubejs.components;

import com.mojang.serialization.Codec;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ScriptRuntime;
import dev.latvian.mods.rhino.type.TypeInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class LevelComponent implements RecipeComponent<ResourceKey<Level>> {
    public static final LevelComponent DIMENSION = new LevelComponent();

    public final String dimension;
    public final Codec<ResourceKey<Level>> codec;

    public LevelComponent() {
        this.dimension = "dimension";
        this.codec = ResourceKey.codec(Registries.DIMENSION);
    }

    @Override
    public Codec<ResourceKey<Level>> codec() {
        return this.codec;
    }

    @Override
    public TypeInfo typeInfo() {
        return TypeInfo.of(ResourceKey.class);
    }

    @Override
    public String toString() {
        return this.dimension;
    }

    @Override
    public ResourceKey<Level> wrap(Context cx, KubeRecipe recipe, Object from) {
        if (from instanceof ResourceKey<?> key) {
            if (key.isFor(Registries.DIMENSION)) {
                return (ResourceKey<Level>) key;
            } else {
                throw ScriptRuntime.typeError(cx, "Expected a ResourceKey for Dimension/Level, but got one for registry: " + key.registry());
            }
        }

        if (from instanceof ResourceLocation rl) {
            return ResourceKey.create(Registries.DIMENSION, rl);
        }

        if (from instanceof String s) {
            ResourceLocation rl = ResourceLocation.tryParse(s);
            if (rl != null) {
                return ResourceKey.create(Registries.DIMENSION, rl);
            } else {
                throw ScriptRuntime.typeError(cx, "Invalid ResourceLocation string for dimension: '" + s + "'");
            }
        }

        throw ScriptRuntime.typeError(cx, "Unable to convert " + from + " (type: " + (from == null ? "null" : from.getClass().getName()) + ") to a Dimension ResourceKey (ResourceKey<Level>)");
    }
}
