package cn.qihuang02.fantasytools.event;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.custom.INV_CLOAK;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = FantasyTools.MODID, value = Dist.CLIENT)
public class ClientPlayerEvents {
    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.@NotNull Pre event) {
        Player player = event.getEntity();
        ItemStack chestArmor = player.getItemBySlot(EquipmentSlot.CHEST);

        if (!chestArmor.isEmpty() && chestArmor.getItem() instanceof INV_CLOAK) {
            event.setCanceled(true);
        }
    }
}
