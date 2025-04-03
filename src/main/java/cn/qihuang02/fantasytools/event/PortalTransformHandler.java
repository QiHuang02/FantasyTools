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
    private static final int MAX_BYPRODUCT_TYPES = 9;

    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        Level level = entity.level();

        if (!(entity instanceof ItemEntity itemEntity) || level.isClientSide() || itemEntity.getItem().isEmpty()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ItemStack inputStack = itemEntity.getItem();
        ResourceKey<Level> currentDimKey = serverLevel.dimension();
        ResourceKey<Level> targetDimKey = event.getDimension();

        RecipeManager recipeManager = serverLevel.getRecipeManager();

        Optional<RecipeHolder<PortalTransformRecipe>> potentiaRecipe = recipeManager.getRecipeFor(
                FTRecipes.PORTAL_TRANSFORM_TYPE.get(),
                new SimpleItemInput(inputStack),
                serverLevel
        );

        Optional<RecipeHolder<PortalTransformRecipe>> finalRecipe = potentiaRecipe.filter(holder -> {
            PortalTransformRecipe recipe = holder.value();

            boolean currentDimMatch = recipe.getRequiredCurrentDimension()
                    .map(required -> required.equals(currentDimKey))
                    .orElse(true);
            boolean targetDimMatch = recipe.getRequiredTargetDimension()
                    .map(required -> required.equals(targetDimKey))
                    .orElse(true);

            return currentDimMatch && targetDimMatch;
        });

        if (finalRecipe.isPresent()) {
            RecipeHolder<PortalTransformRecipe> holder = finalRecipe.get();
            PortalTransformRecipe recipe = holder.value();
            FantasyTools.LOGGER.debug("Found Portal Transform recipe: {}", holder.id());

            event.setCanceled(true);
            executeConversion(itemEntity, serverLevel, recipe);
        }
    }

    /**
     * 根据匹配的配方执行实际的物品转换和副产品生成。
     *
     * @param itemEntity 被转换的原始 ItemEntity。
     * @param level      转换发生的等级。
     * @param recipe     定义转换的匹配 PortalTransformRecipe。
     */
    private static void executeConversion(ItemEntity itemEntity, ServerLevel level, PortalTransformRecipe recipe) {
        Vec3 pos = itemEntity.position();
        Vec3 motion = itemEntity.getDeltaMovement();
        int originalInputCount = itemEntity.getItem().getCount();

        ItemStack outputStack = recipe.getResultItem(level.registryAccess());
        outputStack.setCount(originalInputCount);

        itemEntity.setItem(outputStack);

        if (!recipe.getByproducts().isEmpty()) {

            for (PortalTransformRecipe.ByproductDefinition definition : recipe.getByproducts()) {
                int byproductSpawnedTotalThisType = 0;

                if (definition.minCount() <= 0 || definition.maxCount() < definition.minCount() || definition.template().isEmpty()) {
                    continue;
                }

                for (int index = 0; index < originalInputCount; index++) {
                    if (RANDOM.nextFloat() < definition.chance()) {
                        int countToSpawn;
                        if (definition.maxCount() == definition.minCount()) {
                            countToSpawn = definition.minCount(); // Fixed count
                        } else {
                            countToSpawn = RANDOM.nextInt(definition.minCount(), definition.maxCount() + 1);
                        }

                        if (countToSpawn > 0) {
                            ItemStack byproductStack = definition.template().copy();
                            byproductStack.setCount(countToSpawn);

                            ItemEntity byproductEntity = new ItemEntity(
                                    level,
                                    pos.x(), pos.y(), pos.z(),
                                    byproductStack
                            );
                            byproductEntity.setDeltaMovement(motion.add(
                                    (RANDOM.nextDouble() - 0.5) * 0.1,
                                    RANDOM.nextDouble() * 0.1 + 0.05,
                                    (RANDOM.nextDouble() - 0.5) * 0.1
                            ));
                            level.addFreshEntity(byproductEntity);
                            byproductSpawnedTotalThisType += countToSpawn;
                        }
                    }
                }

                if (byproductSpawnedTotalThisType > 0) {
                    FantasyTools.LOGGER.debug("为配方生成了 {} 个 {} 副产品物品。",
                            byproductSpawnedTotalThisType, definition.template().getItem().getDescriptionId());
                }
            }
        } else {
            FantasyTools.LOGGER.debug("配方未定义副产品。");
        }

        FantasyTools.LOGGER.info("成功使用配方执行了实体 {} 的传送门转换。", itemEntity.getId());
    }
}
