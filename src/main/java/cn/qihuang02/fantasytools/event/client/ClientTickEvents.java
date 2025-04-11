package cn.qihuang02.fantasytools.event.client;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.network.packet.ACTZYPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

@EventBusSubscriber(modid = FantasyTools.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientTickEvents {
    @SubscribeEvent
    public static void registerKeyInput(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        Player player = Minecraft.getInstance().player;

        if (player == null) return;

        while (KeyMappings.ACTIVATE_ZHONGYA_KEY.consumeClick()) {
            ICuriosItemHandler curiosHandler = CuriosApi.getCuriosInventory(player).orElse(null);
            if (curiosHandler != null) {
                for (int i = 0; i < curiosHandler.getEquippedCurios().getSlots(); i++) {
                    ItemStack stack = curiosHandler.getEquippedCurios().getStackInSlot(i);
                    ACTZYPacket packet = new ACTZYPacket(stack);
                    if (packet.isValid()) {
                        PacketDistributor.sendToServer(packet);
                    }
                }
            }
        }
    }
}