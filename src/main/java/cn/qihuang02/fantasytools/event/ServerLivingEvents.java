package cn.qihuang02.fantasytools.event;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.custom.INV_CLOAK;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = FantasyTools.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ServerLivingEvents {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onLivingChangeTarget(@NotNull LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() instanceof Player player) {
            boolean isInvisible = isPlayerInvisible(player);
//            LOGGER.debug("LivingChangeTargetEvent: Entity {} trying to target Player {}. Is Invisible: {}", event.getEntity().getName().getString(), player.getName().getString(), isInvisible);
            if (isInvisible) {
//                LOGGER.debug("Setting target to null for entity {}", event.getEntity().getName().getString());
                event.setNewAboutToBeSetTarget(null);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.@NotNull Post event) {
        if (event.getEntity() instanceof Mob mob && mob.getTarget() != null) {
            if (mob.getTarget() instanceof Player targetPlayer) {
                // 减少检查频率，但确保在需要时检查
                if (mob.tickCount % 10 == 0) { // Check every half second
                    boolean isInvisible = isPlayerInvisible(targetPlayer);
                    if (isInvisible) {
//                        LOGGER.debug("LivingTickEvent: Mob {} target Player {} is now invisible. Clearing target.", mob.getName().getString(), targetPlayer.getName().getString());
                        mob.setTarget(null);
                    }
                }
            }
        }
    }

    private static boolean isPlayerInvisible(@NotNull Player player) {
        ItemStack chestArmor = player.getItemBySlot(EquipmentSlot.CHEST);
        // LOGGER.trace("isPlayerInvisible Check for {}: Has Cloak? {}", player.getName().getString(), hasCloak); // Use trace for very frequent logs
        return !chestArmor.isEmpty() && chestArmor.getItem() instanceof INV_CLOAK;
    }
}
