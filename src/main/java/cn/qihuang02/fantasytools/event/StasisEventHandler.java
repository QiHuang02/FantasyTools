package cn.qihuang02.fantasytools.event;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.util.StasisUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = FantasyTools.MODID)
public class StasisEventHandler {
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (StasisUtil.isInStasis(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBreakEvent(BlockEvent.BreakEvent event) {
        if (StasisUtil.isInStasis(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof LivingEntity entity && StasisUtil.isInStasis(entity)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientTickStart(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (StasisUtil.isInStasis(mc.player)) {
            mc.options.keyJump.setDown(false);
            mc.options.keyShift.setDown(false);
            mc.options.keyAttack.setDown(false);
        }
    }
}
