package cn.qihuang02.fantasytools.event.server;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.custom.Invis_cloak;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = FantasyTools.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerLivingEvents {
    @SubscribeEvent
    public static void onLivingChangeTarget(@NotNull LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        if (mob.getTarget() != null &&
                mob.getTarget().getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof Invis_cloak) {
            mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);

            mob.goalSelector.getAvailableGoals().stream()
                    .map(WrappedGoal::getGoal)
                    .filter(goal -> goal instanceof MeleeAttackGoal)
                    .forEach(goal -> goal.stop());
        }
    }

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.@NotNull Post event) {
        if (event.getEntity() instanceof Mob mob && mob.getTarget() != null) {
            if (mob.getTarget() instanceof Player targetPlayer) {
                boolean isInvisible = isPlayerInvisible(targetPlayer);
                if (isInvisible) {
                    mob.setTarget(null);
                    mob.targetSelector.getAvailableGoals().forEach(WrappedGoal::stop);
                    mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                }
            }
        }
    }

    private static boolean isPlayerInvisible(@NotNull Player player) {
        ItemStack chestArmor = player.getItemBySlot(EquipmentSlot.CHEST);
        return !chestArmor.isEmpty() && chestArmor.getItem() instanceof Invis_cloak;
    }
}
