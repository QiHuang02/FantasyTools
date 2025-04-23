package cn.qihuang02.fantasytools.network;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.network.client.ClientPayloadHandlers;
import cn.qihuang02.fantasytools.network.packet.*;
import cn.qihuang02.fantasytools.network.server.ServerPayloadHandlers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = FantasyTools.MODID, bus = EventBusSubscriber.Bus.MOD)
public class PacketHandler {
    private static final String CHANNEL_VERSION = "1.0.2";

    @SubscribeEvent
    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CHANNEL_VERSION);

        registrar.playToServer(
                ACTZYPacket.TYPE,
                ACTZYPacket.STREAM_CODEC,
                ServerPayloadHandlers::handleACTZY
        );
        registrar.playToServer(
                OpenPocketPacket.TYPE,
                OpenPocketPacket.STREAM_CODEC,
                ServerPayloadHandlers::handleOpenPocket
        );
        registrar.playToServer(
                ChangePocketPagePacket.TYPE,
                ChangePocketPagePacket.STREAM_CODEC,
                ServerPayloadHandlers::handleChangePocketPage
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
        registrar.playToClient(
                SyncPocketPagePacket.TYPE,
                SyncPocketPagePacket.STREAM_CODEC,
                ClientPayloadHandlers::handleSyncPocketPage
        );
    }
}