package cn.qihuang02.fantasytools.network;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.network.client.ClientPayloadHandlers;
import cn.qihuang02.fantasytools.network.packet.ACTZYPacket;
import cn.qihuang02.fantasytools.network.packet.PierceStackEffectPacket;
import cn.qihuang02.fantasytools.network.packet.PierceTriggerEffectPacket;
import cn.qihuang02.fantasytools.network.server.ServerPayloadHandlers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = FantasyTools.MODID, bus = EventBusSubscriber.Bus.MOD)
public class PacketHandler {
    private static final String CHANNEL_VERSION = "1.0.1";

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CHANNEL_VERSION);

        registrar.playToServer(
                ACTZYPacket.TYPE,
                ACTZYPacket.STREAM_CODEC,
                ServerPayloadHandlers::handleACTZY
        );

        registrar.playToClient(
                PierceStackEffectPacket.TYPE,
                PierceStackEffectPacket.STREAM_CODEC,
                ClientPayloadHandlers::handlePierceStackEffect
        );
        registrar.playToClient(
                PierceTriggerEffectPacket.TYPE,
                PierceTriggerEffectPacket.STREAM_CODEC,
                ClientPayloadHandlers::handlePierceTriggerEffect
        );
    }
}