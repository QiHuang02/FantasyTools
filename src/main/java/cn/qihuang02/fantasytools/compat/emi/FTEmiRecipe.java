package cn.qihuang02.fantasytools.compat.emi;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.recipe.custom.PortalTransformRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FTEmiRecipe implements EmiRecipe {
    private RecipeHolder<PortalTransformRecipe> recipeHolder;
    private PortalTransformRecipe recipe;
    private EmiIngredient input;
    private EmiStack output;

    private List<EmiStack> byproductsForDisplay;

    private static final int BYPRODUCT_GRID_X = 34;
    private static final int BYPRODUCT_GRID_Y = 60;
    private static final int GRID_CELL_SIZE = 18;

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

        this.byproductsForDisplay = recipe.byproducts()
                .map(byproducts -> byproducts.stream()
                        .map(def -> EmiStack.of(def.byproduct()))
                        .filter(stack -> !stack.isEmpty())
                        .toList())
                .orElse(List.of());
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
        List<EmiStack> outputs = new ArrayList<>();
        outputs.add(output);

        outputs.addAll(byproductsForDisplay.stream()
                .limit(9)
                .toList());

        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return 120;
    }

    @Override
    public int getDisplayHeight() {
        return  120;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int centerX = (getDisplayWidth() / 2) - 8;

        widgets.addSlot(input, centerX, 2).appendTooltip(() -> createDimensionTooltip(recipe.currentDimension()));
        widgets.addTexture(
                ResourceLocation.fromNamespaceAndPath(FantasyTools.MODID, "textures/gui/transform.png"),
                centerX + 2, 22,
                13, 16,
                0, 0,
                13, 16,
                13, 16
        );
        widgets.addSlot(output, centerX, 40).recipeContext(this).appendTooltip(() -> createDimensionTooltip(recipe.targetDimension()));
        widgets.addTexture(
                ResourceLocation.fromNamespaceAndPath(FantasyTools.MODID, "textures/gui/big_slot.png"),
                34, 60,
                54, 54,
                0, 0,
                54, 54,
                54, 54
        );

        addByproductsSlots(widgets);
    }

    private ClientTooltipComponent createDimensionTooltip(Optional<ResourceKey<Level>> dimensionKey) {
        MutableComponent dimensionText = Component.translatable("tooltip.fantasytools.portal_transform.dimension")
                .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                .append(
                        dimensionKey.map(key -> {
                                    ResourceLocation loc = key.location();
                                    String dimensionKeyStr = "dimension." + loc.getNamespace() + "." + loc.getPath();
                                    return I18n.exists(dimensionKeyStr)
                                            ? Component.translatable(dimensionKeyStr)
                                            : Component.translatable("tooltip.fantasytools.portal_transform.unknown_dimension");
                                })
                                .orElse(Component.translatable("tooltip.fantasytools.portal_transform.no_requirement"))
                                .withStyle(ChatFormatting.GOLD));
        return ClientTooltipComponent.create(dimensionText.getVisualOrderText());
    }



    private void addByproductsSlots(WidgetHolder widgets) {
        int index = 0;
        for (EmiStack byproduct : byproductsForDisplay) {
            int[] pos = getGridPosition(index);

            int finalIndex = index;
            widgets.addSlot(byproduct, pos[0], pos[1])
                    .drawBack(false)
                    .appendTooltip(() ->
                            createMultiLineTooltip(getChanceTooltip(finalIndex)
                            ));
            index++;
        }
    }

    private ClientTooltipComponent createMultiLineTooltip(List<Component> components) {
        List<ClientTooltipComponent> lines = components.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .toList();

        return new ClientTooltipComponent() {
            private static final int LINE_SPACING = 2;

            @Override
            public int getHeight() {
                return lines.stream().mapToInt(ClientTooltipComponent::getHeight).sum() + (lines.size() - 1) * LINE_SPACING;
            }

            @Override
            public int getWidth(Font font) {
                return lines.stream().mapToInt(c -> c.getWidth(font)).max().orElse(0);
            }

            @Override
            public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
                int currentY = y;
                for (ClientTooltipComponent line : lines) {
                    line.renderText(font, x, currentY, matrix, bufferSource);
                    currentY += line.getHeight() + LINE_SPACING;
                }
            }
        };
    }

    private int[] getGridPosition(int index) {
        int row = index / 3;
        int col = index % 3;

        return new int[] {
                BYPRODUCT_GRID_X + col * (GRID_CELL_SIZE),
                BYPRODUCT_GRID_Y + row * (GRID_CELL_SIZE),
        };
    }

    private List<Component> getChanceTooltip(int index) {
        return recipe.byproducts()
                .filter(byproducts -> index < byproducts.size())
                .map(byproducts -> {
                    float chance = byproducts.get(index).chance();
                    int minCount = byproducts.get(index).counts().min();
                    int maxCount = byproducts.get(index).counts().max();
                    return List.<Component>of(
                            Component.translatable("tooltip.fantasytools.portal_transform.byproduct").withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.fantasytools.portal_transform.byproduct.chance", chance * 100).withStyle(ChatFormatting.GRAY),
                            Component.translatable("tooltip.fantasytools.portal_transform.byproduct.min_count", minCount).withStyle(ChatFormatting.DARK_GRAY),
                            Component.translatable("tooltip.fantasytools.portal_transform.byproduct.max_count", maxCount).withStyle(ChatFormatting.DARK_GRAY)
                    );
                })
                .orElse(List.of());
    }
}
