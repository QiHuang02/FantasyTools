package cn.qihuang02.fantasytools.event.server;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.custom.InvisCloak;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = FantasyTools.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerLivingEvents {
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        ItemStack currentChest = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean wearingCloak = !currentChest.isEmpty() && currentChest.getItem() instanceof InvisCloak;

        if (wearingCloak) {
            deaggroNearbyMobs(player);
        }
    }

    private static void deaggroNearbyMobs(Player player) {
        double radius = 40.0;
        List<Mob> nearbyMobs = player.level().getEntitiesOfClass(
                Mob.class,
                player.getBoundingBox().inflate(radius),
                mob -> mob.getTarget() == player
        );
        for (Mob mob : nearbyMobs) {
            mob.setTarget(null);
            mob.targetSelector.getAvailableGoals().forEach(WrappedGoal::stop);
            mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
    }
}
