package cn.qihuang02.fantasytools.event;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.recipe.FTRecipes;
import cn.qihuang02.fantasytools.recipe.custom.PortalTransformRecipe;
import cn.qihuang02.fantasytools.recipe.custom.SimpleItemInput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;

import java.util.Optional;
import java.util.Random;

@EventBusSubscriber(
        modid = FantasyTools.MODID,
        bus = EventBusSubscriber.Bus.GAME)
public class PortalTransformHandler {
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        Level level = entity.level();

        if (
                !(entity instanceof ItemEntity itemEntity) ||
                        level.isClientSide() ||
                        itemEntity.getItem().isEmpty()
        ) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ResourceKey<Level> currentDimKey = serverLevel.dimension();
        ResourceKey<Level> targetDimKey = event.getDimension();

        Optional<RecipeHolder<PortalTransformRecipe>> potentialRecipe = findRecipeByInput(itemEntity, serverLevel);

        potentialRecipe
                .filter(holder -> matchesDimensionRequirements(holder.value(), currentDimKey, targetDimKey))
                .ifPresent(holder -> {
                    event.setCanceled(true);
                    transforming(itemEntity, serverLevel, holder.value());
                });
    }

    /**
     * 根据物品输入查找匹配的 PortalTransformRecipe。
     *
     * @param itemEntity   正在传送的物品实体。
     * @param currentLevel 物品实体当前所在的维度。
     * @return 如果找到基于物品输入的配方，则返回包含 RecipeHolder 的 Optional；否则返回 Optional.empty()。
     */
    private static Optional<RecipeHolder<PortalTransformRecipe>> findRecipeByInput(
            ItemEntity itemEntity,
            ServerLevel currentLevel
    ) {
        ItemStack inputStack = itemEntity.getItem();
        RecipeManager recipeManager = currentLevel.getRecipeManager();
        SimpleItemInput recipeInput = new SimpleItemInput(inputStack);

        return recipeManager.getRecipeFor(
                FTRecipes.PORTAL_TRANSFORM_TYPE.get(),
                recipeInput,
                currentLevel
        );
    }

    private static boolean matchesDimensionRequirements(
            PortalTransformRecipe recipe,
            ResourceKey<Level> currentDimKey,
            ResourceKey<Level> targetDimKey
    ) {
        boolean currentDimMatch =
                recipe
                        .getCurrentDimension()
                        .map(required -> required.equals(currentDimKey))
                        .orElse(true);
        boolean targetDimMatch =
                recipe
                        .getTargetDimension()
                        .map(required -> required.equals(targetDimKey))
                        .orElse(true);

        return currentDimMatch && targetDimMatch;
    }

    /**
     * 根据匹配的配方执行实际的物品转换和副产品生成。
     *
     * @param itemEntity 被转换的原始 ItemEntity。
     * @param level      转换发生的维度。
     * @param recipe     定义转换的匹配 PortalTransformRecipe。
     */
    private static void transforming(ItemEntity itemEntity, ServerLevel level, PortalTransformRecipe recipe) {
        Vec3 pos = itemEntity.position();
        Vec3 motion = itemEntity.getDeltaMovement();
        int originalInputCount = itemEntity.getItem().getCount();

        ItemStack outputStack = recipe.getResultItem(level.registryAccess());
        outputStack.setCount(originalInputCount);

        itemEntity.setItem(outputStack);

        if (!recipe.getByproducts().isEmpty()) {

            for (PortalTransformRecipe.ByproductDefinition definition : recipe.getByproducts()) {
                int byproductSpawnedTotalThisType = 0;

                if (
                        definition.minCount() <= 0 ||
                                definition.maxCount() < definition.minCount() ||
                                definition.byproduct().isEmpty()
                ) {
                    continue;
                }

                for (int index = 0; index < originalInputCount; index++) {
                    if (RANDOM.nextFloat() < definition.chance()) {
                        int countToSpawn;
                        if (definition.maxCount() == definition.minCount()) {
                            countToSpawn = definition.minCount();
                        } else {
                            countToSpawn = RANDOM.nextInt(definition.minCount(), definition.maxCount());
                        }

                        if (countToSpawn > 0) {
                            ItemStack byproductStack = definition.byproduct().copy();
                            byproductStack.setCount(countToSpawn);

                            ItemEntity byproductEntity = new ItemEntity(
                                    level,
                                    pos.x(), pos.y(), pos.z(),
                                    byproductStack
                            );
                            byproductEntity.setDeltaMovement(motion.add(
                                    (level.random.nextFloat() - 0.5) * 0.1,
                                    0.1,
                                    (level.random.nextFloat() - 0.5) * 0.1
                            ));
                            level.addFreshEntity(byproductEntity);
                            byproductSpawnedTotalThisType += countToSpawn;
                        }
                    }
                }
            }
        }
    }
}
