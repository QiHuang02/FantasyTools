package cn.qihuang02.fantasytools.event.client;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.custom.Hourglass;
import cn.qihuang02.fantasytools.network.packet.ACTZYPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;

@EventBusSubscriber(modid = FantasyTools.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientTickEvents {
    @SubscribeEvent
    public static void registerKeyInput(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        if (KeyMappings.ACTIVATE_ZHONGYA_KEY.consumeClick()) {
            CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                handler.getCurios().values().forEach(curios -> {
                    for (int i = 0; i < curios.getSlots(); i++) {
                        ItemStack stack = curios.getStacks().getStackInSlot(i);
                        if (stack.getItem() instanceof Hourglass) {
                            ACTZYPacket packet = new ACTZYPacket(stack);
                            PacketDistributor.sendToServer(packet);
                            FantasyTools.LOGGER.info("Send packet {} to server", stack);
                        }
                    }
                });
            });
        }
    }
}