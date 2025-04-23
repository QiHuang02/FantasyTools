package cn.qihuang02.fantasytools.event.client;

import cn.qihuang02.fantasytools.FantasyTools;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = FantasyTools.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class KeyMappings {
    public static final String KEY_CATEGORY_FANTASYTOOLS = "key.categories." + FantasyTools.MODID;
    public static final String KEY_ACTIVATE_ZHONGYA = "key." + FantasyTools.MODID + ".activate_zhongya";
    public static final String KEY_OPEN_POCKET = "key." + FantasyTools.MODID + ".open_pocket";

    public static final KeyMapping ACTIVATE_ZHONGYA_KEY = new KeyMapping(
            KEY_ACTIVATE_ZHONGYA,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            KEY_CATEGORY_FANTASYTOOLS
    );

    public static final KeyMapping OPEN_POCKET_KEY = new KeyMapping(
            KEY_OPEN_POCKET,
            KeyConflictContext.IN_GAME, // Only active when in game world
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P, // Default key 'P'
            KEY_CATEGORY_FANTASYTOOLS // Assign to our mod's category
    );

    @SubscribeEvent
    public static void onRegisterKeyBinds(RegisterKeyMappingsEvent event) {
        event.register(ACTIVATE_ZHONGYA_KEY);
    }
}
