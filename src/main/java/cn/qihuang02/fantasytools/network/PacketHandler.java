package cn.qihuang02.fantasytools.network;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.network.packet.ACTZYPacket;
import cn.qihuang02.fantasytools.network.server.ServerPayloadHandlers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = FantasyTools.MODID, bus = EventBusSubscriber.Bus.MOD)
public class PacketHandler {
    private static final String CHANNEL_VERSION = "1.0.0";

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CHANNEL_VERSION);

        registrar.playToServer(
                ACTZYPacket.TYPE,
                ACTZYPacket.STREAM_CODEC,
                ServerPayloadHandlers::handleACTZY
        );
    }
}