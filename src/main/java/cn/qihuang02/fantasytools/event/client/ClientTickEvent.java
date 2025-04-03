package cn.qihuang02.fantasytools.event.client;

import cn.qihuang02.fantasytools.FantasyTools;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@EventBusSubscriber(modid = FantasyTools.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ClientTickEvent {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void registerKeyInput(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
        if (KeyMappings.ACTIVATE_ZHONGYA_KEY.consumeClick()) {
            LOGGER.debug("Activate Zhongya Key");
        }
    }
}
