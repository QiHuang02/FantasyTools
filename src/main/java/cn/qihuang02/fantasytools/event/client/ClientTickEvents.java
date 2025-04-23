package cn.qihuang02.fantasytools.event.client;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.custom.FourDimensionalPocket;
import cn.qihuang02.fantasytools.network.packet.ACTZYPacket;
import cn.qihuang02.fantasytools.network.packet.OpenPocketPacket;
import cn.qihuang02.fantasytools.util.HourglassUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
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
            ACTZYPacket packet = new ACTZYPacket(hourglassToActivate);
            PacketDistributor.sendToServer(packet);
        } else {
            FantasyTools.LOGGER.debug("Client:Press the activation key, but no available hourglass was found (maybe missing or are all in cooling).");
        }

        if (KeyMappings.OPEN_POCKET_KEY.consumeClick()) {
            InteractionHand handHoldingPocket = null;
            ItemStack mainHandStack = player.getMainHandItem();
            ItemStack offHandStack = player.getOffhandItem();

            if (mainHandStack.getItem() instanceof FourDimensionalPocket) {
                handHoldingPocket = InteractionHand.MAIN_HAND;
            } else if (offHandStack.getItem() instanceof FourDimensionalPocket) {
                handHoldingPocket = InteractionHand.OFF_HAND;
            }

            if (handHoldingPocket != null) {
                FantasyTools.LOGGER.debug("Client: Sending OpenPocketPacket via keybind for hand {}", handHoldingPocket);
                OpenPocketPacket packet = new OpenPocketPacket(handHoldingPocket);
                PacketDistributor.sendToServer(packet);
            } else {
                FantasyTools.LOGGER.debug("Client: Pocket key pressed, but no pocket found in hands.");
                // Optional: Add feedback to player? e.g., mc.gui.getChat().addMessage(...)
            }
        }
    }
}