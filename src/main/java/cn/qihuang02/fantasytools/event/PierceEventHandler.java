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

        ResourceKey<Enchantment> enchantmentResourceKey = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(FantasyTools.MODID, "pierce"));
        Registry<Enchantment> enchantmentRegistry = targetEntity.level().registryAccess().registryOrThrow(Registries.ENCHANTMENT);
        Optional<Holder.Reference<Enchantment>> optionalHolder = enchantmentRegistry.getHolder(enchantmentResourceKey);

        Holder.Reference<Enchantment> holder = optionalHolder.get();
        ItemStack weapon = player.getMainHandItem();
        int enchantmentLevel = EnchantmentHelper.getTagEnchantmentLevel(holder, weapon);
        if (enchantmentLevel <= 0) {
            return;
        }

        Map<UUID, SpearAttachments.SpearData> originalMap = targetEntity.getData(SpearAttachments.SPEARS);
        Map<UUID, SpearAttachments.SpearData> spearMap = new HashMap<>(originalMap);
        UUID attackerUUID = player.getUUID();
        long currentTick = targetEntity.level().getGameTime();

        if (player.isShiftKeyDown()) {
            FantasyTools.LOGGER.debug("Shift + Attack detected for Pierce.");
            int currentSpearCount = spearMap.getOrDefault(attackerUUID, new SpearAttachments.SpearData(0, currentTick)).count();
            float bonusDamage = calculateBonusDamage(enchantmentLevel, targetEntity, currentSpearCount);
            float originalDamage = event.getOriginalDamage();
            event.setNewDamage(originalDamage + bonusDamage);
            FantasyTools.LOGGER.debug("Pierce Shift-Triggered! Spears={}, OriginalDmg={}, BonusDmg={}, NewAmount={}", currentSpearCount, originalDamage, bonusDamage, event.getOriginalDamage());
            if (spearMap.containsKey(attackerUUID)) {
                spearMap.remove(attackerUUID);
                FantasyTools.LOGGER.debug("Spears removed via Shift-Trigger for {} on {}", player.getName().getString(), targetEntity.getName().getString());
            } else {
                FantasyTools.LOGGER.debug("No existing spears to remove via Shift-Trigger for {} on {}", player.getName().getString(), targetEntity.getName().getString());
            }
            targetEntity.setData(SpearAttachments.SPEARS, spearMap);
            return;
        }

        SpearAttachments.SpearData currentData = spearMap.getOrDefault(attackerUUID, new SpearAttachments.SpearData(0, currentTick));
        int newSpearCount = currentData.count() + 1;
        int threshold = FTEnchantments.getSpearThreshold(enchantmentLevel);
        boolean thresholdReached = newSpearCount >= threshold;
        if (thresholdReached) {
            float bonusDamage = calculateBonusDamage(enchantmentLevel, targetEntity, newSpearCount);
            spearMap.remove(attackerUUID);
            float originalDamage = event.getOriginalDamage();
            event.setNewDamage(originalDamage + bonusDamage);
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
            FantasyTools.LOGGER.debug("Updated spear data for entity {} after expiration check.", entity.getUUID());
        }
    }
}
