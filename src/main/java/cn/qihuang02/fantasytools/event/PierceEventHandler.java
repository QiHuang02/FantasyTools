package cn.qihuang02.fantasytools.event;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.attachment.SpearAttachments;
import cn.qihuang02.fantasytools.enchantment.FTEnchantments;
import cn.qihuang02.fantasytools.network.packet.PierceStackEffectPacket;
import cn.qihuang02.fantasytools.network.packet.PierceTriggerEffectPacket;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
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
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = FantasyTools.MODID, bus = EventBusSubscriber.Bus.GAME)
public class PierceEventHandler {
    private static final int EXPIRATION_TICKS = 5 * 20;
    private static final int INVULNERABILITY_TICKS = 10;

    private static final Map<UUID, PendingPierceDamage> pendingDamageMap = new ConcurrentHashMap<>();

    private record PendingPierceDamage(UUID attackerUUID, float damageAmount, long scheduledTick, int stackCountForEffect) {}

    /**
     * Handles logic *after* a LivingEntity takes damage.
     * Checks for Pierce enchantment, manages stacks, and schedules bonus damage if triggered.
     */
    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) ||
                !(event.getEntity() instanceof LivingEntity targetEntity) ||
                targetEntity.level().isClientSide) {
            return;
        }

        int enchantmentLevel = getEnchantmentLevel(player);
        if (enchantmentLevel <= 0) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) targetEntity.level();
        UUID attackerUUID = player.getUUID();
        Map<UUID, SpearAttachments.SpearData> spearMap = getSpearMap(targetEntity);
        long currentTick = serverLevel.getGameTime();

        if (player.isShiftKeyDown()) {
            handleShiftAttack(player, targetEntity, spearMap, attackerUUID, enchantmentLevel, serverLevel, currentTick);
        }
        else {
            handleNormalAttack(player, targetEntity, spearMap, attackerUUID, enchantmentLevel, serverLevel, currentTick);
        }
    }

    /**
     * Handles entity ticks. Used for expiring old Pierce stacks and applying scheduled bonus damage.
     */
    @SubscribeEvent
    public static void onEntityTickPost(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        // Only run on server for LivingEntities
        if (!(entity instanceof LivingEntity livingEntity) || entity.level().isClientSide()) {
            return;
        }

        long currentTick = livingEntity.level().getGameTime();

        applyPendingDamage(livingEntity, currentTick);

        expireOldStacks(livingEntity, currentTick);
    }


    /**
     * Processes a Shift+Attack, consuming existing stacks to schedule Pierce damage.
     */
    private static void handleShiftAttack(ServerPlayer player, LivingEntity targetEntity, Map<UUID, SpearAttachments.SpearData> spearMap, UUID attackerUUID, int enchantmentLevel, ServerLevel serverLevel, long currentTick) {
        SpearAttachments.SpearData currentData = spearMap.getOrDefault(attackerUUID, new SpearAttachments.SpearData(0, currentTick));
        int currentSpearCount = currentData.count();

        if (currentSpearCount > 0) {
            FantasyTools.LOGGER.debug("Shift+Attack Pierce trigger: Player {} on Entity {} with {} stacks.", player.getName().getString(), targetEntity.getName().getString(), currentSpearCount);
            float bonusDamage = calculateBonusDamage(enchantmentLevel, targetEntity, currentSpearCount);

            schedulePierceDamage(targetEntity, attackerUUID, bonusDamage, currentTick, currentSpearCount);

            spearMap.remove(attackerUUID);
            targetEntity.setData(SpearAttachments.SPEARS, spearMap);

            PacketDistributor.sendToPlayersTrackingEntity(targetEntity, new PierceTriggerEffectPacket(targetEntity.getId(), currentSpearCount));
        } else {
            FantasyTools.LOGGER.debug("Shift+Attack Pierce: Player {} on Entity {} with 0 stacks, no effect.", player.getName().getString(), targetEntity.getName().getString());
        }
    }

    /**
     * Processes a Normal Attack, adding a stack or consuming stacks if the threshold is met.
     */
    private static void handleNormalAttack(ServerPlayer player, LivingEntity targetEntity, Map<UUID, SpearAttachments.SpearData> spearMap, UUID attackerUUID, int enchantmentLevel, ServerLevel serverLevel, long currentTick) {
        SpearAttachments.SpearData currentData = spearMap.getOrDefault(attackerUUID, new SpearAttachments.SpearData(0, currentTick));
        int newSpearCount = currentData.count() + 1;
        int threshold = FTEnchantments.getSpearThreshold(enchantmentLevel);

        if (threshold > 0 && newSpearCount >= threshold) {
            FantasyTools.LOGGER.debug("Normal Attack Pierce trigger (threshold {} met): Player {} on Entity {} with {} stacks.", threshold, player.getName().getString(), targetEntity.getName().getString(), newSpearCount);
            float bonusDamage = calculateBonusDamage(enchantmentLevel, targetEntity, newSpearCount);

            schedulePierceDamage(targetEntity, attackerUUID, bonusDamage, currentTick, newSpearCount);

            spearMap.remove(attackerUUID);
            targetEntity.setData(SpearAttachments.SPEARS, spearMap);

            PacketDistributor.sendToPlayersTrackingEntity(targetEntity, new PierceTriggerEffectPacket(targetEntity.getId(), newSpearCount));
        }
        else {
            spearMap.put(attackerUUID, new SpearAttachments.SpearData(newSpearCount, currentTick));
            targetEntity.setData(SpearAttachments.SPEARS, spearMap);
            FantasyTools.LOGGER.debug("Normal Attack Pierce stack added: Player {} on Entity {}, count: {}", player.getName().getString(), targetEntity.getName().getString(), newSpearCount);

            PacketDistributor.sendToPlayersTrackingEntity(targetEntity, new PierceStackEffectPacket(targetEntity.getId()));
        }
    }

    /**
     * Schedules the Pierce bonus damage to be applied after the invulnerability period.
     */
    private static void schedulePierceDamage(LivingEntity targetEntity, UUID attackerUUID, float damageAmount, long currentTick, int stackCount) {
        if (damageAmount <= 0) return;

        long scheduledTick = currentTick + INVULNERABILITY_TICKS + 1;
        PendingPierceDamage pendingDamage = new PendingPierceDamage(attackerUUID, damageAmount, scheduledTick, stackCount);

        pendingDamageMap.put(targetEntity.getUUID(), pendingDamage);
        FantasyTools.LOGGER.debug("Scheduled Pierce damage ({}) for entity {} on tick {}", damageAmount, targetEntity.getName().getString(), scheduledTick);
    }

    /**
     * Applies any pending Pierce damage scheduled for the current tick.
     */
    private static void applyPendingDamage(LivingEntity livingEntity, long currentTick) {
        PendingPierceDamage pendingDamage = pendingDamageMap.get(livingEntity.getUUID());

        if (pendingDamage != null && currentTick >= pendingDamage.scheduledTick()) {
            pendingDamageMap.remove(livingEntity.getUUID());

            if (livingEntity.isAlive() && pendingDamage.damageAmount > 0) {
                FantasyTools.LOGGER.debug("Applying scheduled Pierce damage ({}) to entity {} from attacker {}", pendingDamage.damageAmount, livingEntity.getName().getString(), pendingDamage.attackerUUID);

                livingEntity.hurt(livingEntity.damageSources().magic(), pendingDamage.damageAmount);

            } else {
                FantasyTools.LOGGER.debug("Skipped applying scheduled Pierce damage to entity {} (dead or zero damage).", livingEntity.getName().getString());
            }
        }
    }

    /**
     * Removes expired Pierce stacks from the entity's attachment data.
     */
    private static void expireOldStacks(LivingEntity livingEntity, long currentTick) {
        if (!livingEntity.hasData(SpearAttachments.SPEARS)) {
            return;
        }

        Map<UUID, SpearAttachments.SpearData> originalMap = livingEntity.getData(SpearAttachments.SPEARS);
        Map<UUID, SpearAttachments.SpearData> mutableMap = new HashMap<>(originalMap);

        if (mutableMap.isEmpty()) {
            return;
        }

        boolean changed = false;
        Iterator<Map.Entry<UUID, SpearAttachments.SpearData>> iterator = mutableMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, SpearAttachments.SpearData> entry = iterator.next();
            SpearAttachments.SpearData data = entry.getValue();

            if (currentTick - data.lastAttackTick() > EXPIRATION_TICKS) {
                iterator.remove();
                changed = true;
                FantasyTools.LOGGER.debug("Expired Pierce stacks for attacker {} on entity {}", entry.getKey(), livingEntity.getName().getString());
            }
        }

        if (changed) {
            livingEntity.setData(SpearAttachments.SPEARS, Collections.unmodifiableMap(mutableMap)); // Set data with the modified map
        }
    }


    /**
     * Gets the Pierce enchantment level from the player's main hand item.
     */
    private static int getEnchantmentLevel(ServerPlayer player) {
        Optional<Holder.Reference<Enchantment>> optionalHolder = player.level().registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(FTEnchantments.PIERCE);

        if (optionalHolder.isPresent()) {
            ItemStack weapon = player.getMainHandItem();
            return EnchantmentHelper.getTagEnchantmentLevel(optionalHolder.get(), weapon);
        }
        return 0;
    }

    /**
     * Retrieves a mutable copy of the spear attachment data for the target entity.
     */
    public static Map<UUID, SpearAttachments.SpearData> getSpearMap(LivingEntity targetEntity) {
        return new HashMap<>(targetEntity.getData(SpearAttachments.SPEARS));
    }

    /**
     * Calculates the bonus Pierce damage based on enchantment level, target health, and stack count.
     * (Using the formula you provided in the prompt)
     */
    private static float calculateBonusDamage(int enchantmentLevel, LivingEntity targetEntity, int spearCount) {
        if (spearCount <= 0) return 0.0f;

        float baseDamageFactor = 0.1f;
        float scalingFactor = 0.015f;
        float levelMultiplierFactor = 0.2f;
        int percentThreshold = 5;
        float percentPerStackAboveThreshold = 0.002f * enchantmentLevel;
        float maxPercentBonus = 0.2f;

        float levelMultiplier = 1.0f + levelMultiplierFactor * (enchantmentLevel - 1);

        float stackBasedDamage = (baseDamageFactor * spearCount + scalingFactor * spearCount * spearCount) * levelMultiplier;

        float percentHealthDamage = 0f;
        if (spearCount > percentThreshold) {
            float currentPercent = percentPerStackAboveThreshold * (spearCount - percentThreshold);
            currentPercent = Math.min(currentPercent, maxPercentBonus);
            percentHealthDamage = targetEntity.getMaxHealth() * currentPercent;
        }

        FantasyTools.LOGGER.debug("Calculated Pierce Bonus Damage: Level={}, Stacks={}, StackDmg={}, PercentDmg={}, Total={}",
                enchantmentLevel, spearCount, stackBasedDamage, percentHealthDamage, stackBasedDamage + percentHealthDamage);

        return stackBasedDamage + percentHealthDamage;
    }
}