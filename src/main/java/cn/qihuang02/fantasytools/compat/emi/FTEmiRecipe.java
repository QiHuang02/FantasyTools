package cn.qihuang02.fantasytools.compat.emi;

import cn.qihuang02.fantasytools.recipe.custom.PortalTransformRecipe;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FTEmiRecipe implements EmiRecipe {
    private RecipeHolder<PortalTransformRecipe> recipeHolder;
    private PortalTransformRecipe recipe;
    private EmiIngredient input;
    private EmiStack output;

    private List<EmiStack> byproductsForDisplay;

    public FTEmiRecipe(RecipeHolder<PortalTransformRecipe> holder) {
        this.recipeHolder = holder;
        this.recipe = holder.value();
        this.input = EmiIngredient.of(recipe.inputIngredient());

        HolderLookup.Provider registries = null;
        if (Minecraft.getInstance().level != null) {
            registries = Minecraft.getInstance().level.registryAccess();
        }
        if (registries != null) {
            this.output = EmiStack.of(recipe.getResultItem(registries));
        }

        this.byproductsForDisplay = recipe.byproducts().stream()
                .map(def -> EmiStack.of(def.template()))
                .filter(stack -> !stack.isEmpty())
                .toList();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return FTEmiClientPlugin.PORTAL_TRANSFORM_CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return recipeHolder.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(output);
        // List<EmiStack> allOutputs = new java.util.ArrayList<>();
        // allOutputs.add(output);
        // allOutputs.addAll(byproductsForDisplay);
        // return allOutputs;
    }

    @Override
    public int getDisplayWidth() {
        return 82;
    }

    @Override
    public int getDisplayHeight() {
        return 36;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int x = 0;
        int y = (getDisplayHeight() - 18) / 2;

        widgets.addSlot(input, x, y);
        x += 18 + 4; // 输入槽宽度 + 间距

        widgets.addTexture(EmiTexture.EMPTY_ARROW, x, y + 1);
        x += EmiTexture.EMPTY_ARROW.width + 4;

        widgets.addSlot(output, x, y).recipeContext(this);

        int byproductX = x;
        int byproductY = y + 20;
        for (EmiStack byproduct : byproductsForDisplay) {
            widgets.addSlot(byproduct, byproductX, byproductY);
            // 添加 Tooltip 显示几率和数量
            widgets.addTooltipText(List.of(Component.translatable("emi.fantasytools.byproduct_info," /*,  添加几率、数量信息 */)), byproductX, byproductY, 18, 18);
            byproductY += 10; // 稍微向下移动下一个副产品槽位
            if (byproductY > getDisplayHeight() - 18) { // 避免超出边界
                byproductX += 20;
                byproductY = y + 20;
            }
        }
    }
}
