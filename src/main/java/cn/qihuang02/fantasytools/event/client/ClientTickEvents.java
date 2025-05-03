package cn.qihuang02.fantasytools.event.client;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.network.packet.ACTZYPacket;
import cn.qihuang02.fantasytools.util.HourglassUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = FantasyTools.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientTickEvents {
    @SubscribeEvent
    public static void registerKeyInput(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null || !KeyMappings.ACTIVATE_ZHONGYA_KEY.consumeClick()) {
            return;
        }

        ItemStack hourglassToActivate = HourglassUtils.findFirstEligibleHourglassClient(player);

        if (!hourglassToActivate.isEmpty()) {
            FantasyTools.LOGGER.debug("Client:Find the available hourglass {} and prepare to send the packet.", hourglassToActivate.getDisplayName().getString());
            ACTZYPacket packet = new ACTZYPacket();
            PacketDistributor.sendToServer(packet);
        } else {
            FantasyTools.LOGGER.debug("Client:Press the activation key, but no available hourglass was found (maybe missing or are all in cooling).");
        }
    }
}