package cn.qihuang02.fantasytools.event;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.attachment.SpearAttachments;
import cn.qihuang02.fantasytools.enchantment.FTEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.*;

@EventBusSubscriber(modid = FantasyTools.MODID, bus = EventBusSubscriber.Bus.GAME)
public class PierceEventHandler {
    private static final int EXPIRATION_TICKS = 5 * 20;

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) ||
                !(event.getEntity() instanceof LivingEntity targetEntity) ||
                targetEntity.level().isClientSide) {
            return;
        }

        int enchantmentLevel = getEnchantmentLevel(targetEntity, player);
        if (enchantmentLevel <= 0) {
            return;
        }

        Map<UUID, SpearAttachments.SpearData> spearMap = getSpearMap(targetEntity);
        UUID attackerUUID = player.getUUID();
        long currentTick = targetEntity.level().getGameTime();

        if (player.isCrouching()) {
            handleShiftAttack(event, spearMap, attackerUUID, enchantmentLevel, targetEntity);
            return;
        }

        handleNormalAttack(event, spearMap, attackerUUID, enchantmentLevel, targetEntity, currentTick);
    }

    private static int getEnchantmentLevel(LivingEntity targetEntity, ServerPlayer player) {
        ResourceKey<Enchantment> enchantmentResourceKey = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(FantasyTools.MODID, "pierce"));
        Registry<Enchantment> enchantmentRegistry = targetEntity.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        Optional<Holder.Reference<Enchantment>> optionalHolder = enchantmentRegistry.getHolder(enchantmentResourceKey);
        Holder.Reference<Enchantment> holder = optionalHolder.get();
        ItemStack weapon = player.getMainHandItem();
        return EnchantmentHelper.getTagEnchantmentLevel(holder, weapon);
    }

    public static Map<UUID, SpearAttachments.SpearData> getSpearMap(LivingEntity targetEntity) {
        return new HashMap<>(targetEntity.getData(SpearAttachments.SPEARS));
    }

    private static void handleShiftAttack(LivingDamageEvent.Pre event, Map<UUID, SpearAttachments.SpearData> spearMap, UUID attackerUUID, int enchantmentLevel, LivingEntity targetEntity) {
        FantasyTools.LOGGER.debug("Shift + Attack detected for Pierce.");
        int currentSpearCount = spearMap.getOrDefault(attackerUUID, new SpearAttachments.SpearData(0, targetEntity.level().getGameTime())).count();
        float bonusDamage = calculateBonusDamage(enchantmentLevel, targetEntity, currentSpearCount);
        event.setNewDamage(event.getOriginalDamage() + bonusDamage);
        spearMap.remove(attackerUUID);
        targetEntity.setData(SpearAttachments.SPEARS, spearMap);
    }

    private static void handleNormalAttack(LivingDamageEvent.Pre event, Map<UUID, SpearAttachments.SpearData> spearMap, UUID attackerUUID, int enchantmentLevel, LivingEntity targetEntity, long currentTick) {
        SpearAttachments.SpearData currentData = spearMap.getOrDefault(attackerUUID, new SpearAttachments.SpearData(0, currentTick));
        int newSpearCount = currentData.count() + 1;
        int threshold = FTEnchantments.getSpearThreshold(enchantmentLevel);
        if (newSpearCount >= threshold) {
            float bonusDamage = calculateBonusDamage(enchantmentLevel, targetEntity, newSpearCount);
            spearMap.remove(attackerUUID);

            targetEntity.hurt(targetEntity.damageSources().magic(), bonusDamage);
        } else {
            spearMap.put(attackerUUID, new SpearAttachments.SpearData(newSpearCount, currentTick));
        }
        targetEntity.setData(SpearAttachments.SPEARS, spearMap);
    }

    private static float calculateBonusDamage(int enchantmentLevel, LivingEntity targetEntity, int spearCount) {
        float baseBonus = 0.05f * enchantmentLevel;
        int effectiveSpearCount = Math.max(0, spearCount);
        float percentHealthBonus = (targetEntity.getMaxHealth() * 0.01f * enchantmentLevel) + effectiveSpearCount;
        return baseBonus + percentHealthBonus;
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity) || entity.level().isClientSide() || !(entity.hasData(SpearAttachments.SPEARS))) {
            return;
        }

        Map<UUID, SpearAttachments.SpearData> originalMap = entity.getData(SpearAttachments.SPEARS);
        Map<UUID, SpearAttachments.SpearData> spearDataMap = new HashMap<>(originalMap);
        if (spearDataMap.isEmpty()) {
            return;
        }

        long currentTick = entity.level().getGameTime();
        boolean changed = false;

        Iterator<Map.Entry<UUID, SpearAttachments.SpearData>> iterator = spearDataMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, SpearAttachments.SpearData> entry = iterator.next();
            SpearAttachments.SpearData data = entry.getValue();

            if (currentTick - data.lastAttackTick() > EXPIRATION_TICKS) {
                iterator.remove();
                changed = true;
            }
        }

        if (changed) {
            entity.setData(SpearAttachments.SPEARS, spearDataMap);
        }
    }
}
