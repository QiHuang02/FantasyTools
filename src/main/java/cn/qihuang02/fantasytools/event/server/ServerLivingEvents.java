package cn.qihuang02.fantasytools.event.server;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.custom.Invis_cloak;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
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
        if (event.getNewAboutToBeSetTarget() instanceof Player player) {
            boolean isInvisible = isPlayerInvisible(player);
            if (isInvisible) {
                event.setNewAboutToBeSetTarget(null);
            }
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
