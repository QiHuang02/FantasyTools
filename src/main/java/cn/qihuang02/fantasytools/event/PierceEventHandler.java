package cn.qihuang02.fantasytools.event;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.attachment.SpearAttachments;
import cn.qihuang02.fantasytools.enchantment.FTEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
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

        ServerLevel serverLevel = null;
        if (targetEntity.level() instanceof ServerLevel sl) {
            serverLevel = sl;
        }

        if (player.isCrouching()) {
            handleShiftAttack(event, spearMap, attackerUUID, enchantmentLevel, targetEntity, serverLevel);
            return;
        }

        handleNormalAttack(event, spearMap, attackerUUID, enchantmentLevel, targetEntity, currentTick, serverLevel);
    }

    private static int getEnchantmentLevel(LivingEntity entity, ServerPlayer player) {
        Optional<Holder.Reference<Enchantment>> optionalHolder = player.level().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(FTEnchantments.PIERCE);

        if (optionalHolder.isPresent()) {
            ItemStack weapon = player.getMainHandItem();
            return EnchantmentHelper.getTagEnchantmentLevel(optionalHolder.get(), weapon);
        }
        return 0;
    }

    public static Map<UUID, SpearAttachments.SpearData> getSpearMap(LivingEntity targetEntity) {
        return new HashMap<>(targetEntity.getData(SpearAttachments.SPEARS));
    }

    private static void handleShiftAttack(
            LivingDamageEvent.Pre event,
            Map<UUID, SpearAttachments.SpearData> spearMap,
            UUID attackerUUID,
            int enchantmentLevel,
            LivingEntity targetEntity,
            ServerLevel serverLevel
    ) {
        FantasyTools.LOGGER.debug("Shift + Attack detected for Pierce.");
        int currentSpearCount = spearMap.getOrDefault(attackerUUID, new SpearAttachments.SpearData(0, targetEntity.level().getGameTime())).count();
        float bonusDamage = calculateBonusDamage(enchantmentLevel, targetEntity, currentSpearCount);

        spearMap.remove(attackerUUID);
        targetEntity.setData(SpearAttachments.SPEARS, spearMap);

        if (serverLevel != null && currentSpearCount > 0) {
            serverLevel.sendParticles(
                    ParticleTypes.SWEEP_ATTACK,
                    targetEntity.getX(),
                    targetEntity.getY(0.6),
                    targetEntity.getZ(),
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
            serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                    targetEntity.getX(),
                    targetEntity.getY(0.5) + targetEntity.getBbHeight() / 2.0, // 实体顶部
                    targetEntity.getZ(),
                    Math.min(currentSpearCount, 10),
                    targetEntity.getBbWidth() / 2.0,
                    targetEntity.getBbHeight() / 3.0,
                    targetEntity.getBbWidth() / 2.0,
                    0.1);
            serverLevel.playSound(null,
                    targetEntity.getX(), targetEntity.getY(), targetEntity.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP,
                    targetEntity.getSoundSource(),
                    1.0F,
                    0.8F + serverLevel.random.nextFloat() * 0.4F);
        }

        event.setNewDamage(event.getOriginalDamage() + bonusDamage);
    }

    private static void handleNormalAttack(
            LivingDamageEvent.Pre event,
            Map<UUID, SpearAttachments.SpearData> spearMap,
            UUID attackerUUID,
            int enchantmentLevel,
            LivingEntity targetEntity,
            long currentTick,
            ServerLevel serverLevel
    ) {
        SpearAttachments.SpearData currentData = spearMap.getOrDefault(attackerUUID, new SpearAttachments.SpearData(0, currentTick));
        int newSpearCount = currentData.count() + 1;
        int threshold = FTEnchantments.getSpearThreshold(enchantmentLevel);

        if (newSpearCount >= threshold) {
            float bonusDamage = calculateBonusDamage(enchantmentLevel, targetEntity, newSpearCount);

            spearMap.remove(attackerUUID);
            targetEntity.setData(SpearAttachments.SPEARS, spearMap);

            if (serverLevel != null) {
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        targetEntity.getX(),
                        targetEntity.getY(0.7),
                        targetEntity.getZ(),
                        10 + newSpearCount,
                        targetEntity.getBbWidth() / 2.0,
                        targetEntity.getBbHeight() / 2.0,
                        targetEntity.getBbWidth() / 2.0,
                        0.3);
                serverLevel.playSound(null,
                        targetEntity.getX(), targetEntity.getY(), targetEntity.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT,
                        targetEntity.getSoundSource(),
                        2.0F,
                        1.0F);
            }
            targetEntity.hurt(targetEntity.damageSources().magic(), bonusDamage);

        } else {
            spearMap.put(attackerUUID, new SpearAttachments.SpearData(newSpearCount, currentTick));
            targetEntity.setData(SpearAttachments.SPEARS, spearMap);

            if (serverLevel != null) {
                serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                        targetEntity.getRandomX(0.5),
                        targetEntity.getRandomY(),
                        targetEntity.getRandomZ(0.5),
                        3,
                        0.1, 0.1, 0.1,
                        0.02);

                serverLevel.playSound(null,
                        targetEntity.getX(), targetEntity.getY(), targetEntity.getZ(),
                        SoundEvents.ARROW_HIT_PLAYER,
                        targetEntity.getSoundSource(),
                        2.0F,
                        1.5F + serverLevel.random.nextFloat() * 0.5F);
            }
        }
    }

    private static float calculateBonusDamage(int enchantmentLevel, LivingEntity targetEntity, int spearCount) {
        float baseBonus = 0.05f * enchantmentLevel;
        int effectiveSpearCount = Math.max(1, spearCount);
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
        if (originalMap.isEmpty()) return;
        Map<UUID, SpearAttachments.SpearData> spearDataMap = new HashMap<>(originalMap);
        if (spearDataMap.isEmpty()) return;

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
