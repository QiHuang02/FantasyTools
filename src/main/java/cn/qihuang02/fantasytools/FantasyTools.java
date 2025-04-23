package cn.qihuang02.fantasytools;

import cn.qihuang02.fantasytools.attachment.SpearAttachments;
import cn.qihuang02.fantasytools.client.gui.PocketScreen;
import cn.qihuang02.fantasytools.component.FTComponents;
import cn.qihuang02.fantasytools.effect.FTEffect;
import cn.qihuang02.fantasytools.enchantment.FTEnchantmentEffects;
import cn.qihuang02.fantasytools.item.FTCreativeModeTabs;
import cn.qihuang02.fantasytools.item.FTItems;
import cn.qihuang02.fantasytools.menu.FTMenuTypes;
import cn.qihuang02.fantasytools.recipe.FTRecipes;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

@Mod(FantasyTools.MODID)
public class FantasyTools {
    public static final String MODID = "fantasytools";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FantasyTools(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        FTEffect.register(modEventBus);
        FTItems.register(modEventBus);
        FTCreativeModeTabs.register(modEventBus);
        FTComponents.register(modEventBus);

        FTEnchantmentEffects.register(modEventBus);
        SpearAttachments.register(modEventBus);

        FTRecipes.register(modEventBus);

        FTMenuTypes.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, FTConfig.SPEC, String.format("%s-common.toml", MODID));

        NeoForge.EVENT_BUS.register(this);
    }

    public static ResourceLocation getRL(String path) {
        return ResourceLocation.fromNamespaceAndPath(FantasyTools.MODID, path);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        @SubscribeEvent
        public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
            event.register(FTMenuTypes.POCKET_MENU_TYPE.get(), PocketScreen::new);
        }
    }
}
