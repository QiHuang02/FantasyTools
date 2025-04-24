package cn.qihuang02.fantasytools.event.client;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.custom.InvisCloak;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = FantasyTools.MODID, value = Dist.CLIENT)
public class ClientPlayerEvents {
    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.@NotNull Pre<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {
//        Player player = Minecraft.getInstance().player;
//        if (player == null) {
//            return;
//        }

        var livingEntity = event.getEntity();
        if (livingEntity instanceof Player) {
            ItemStack chestArmor = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
            if (!chestArmor.isEmpty() && chestArmor.getItem() instanceof InvisCloak /* && livingEntity.isInvisibleTo(player) */) {
                event.setCanceled(true);
            }
        }
    }
}
