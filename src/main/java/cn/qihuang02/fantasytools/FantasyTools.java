package cn.qihuang02.fantasytools;

import cn.qihuang02.fantasytools.attachment.SpearAttachments;
import cn.qihuang02.fantasytools.component.FTComponents;
import cn.qihuang02.fantasytools.effect.FTEffect;
import cn.qihuang02.fantasytools.enchantment.FTEnchantmentEffects;
import cn.qihuang02.fantasytools.item.FTCreativeModeTabs;
import cn.qihuang02.fantasytools.item.FTItems;
import cn.qihuang02.fantasytools.recipe.FTRecipes;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(FantasyTools.MODID)
public class FantasyTools {
    public static final String MODID = "fantasytools";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FantasyTools(IEventBus modEventBus, ModContainer modContainer) {
        FTEffect.register(modEventBus);
        FTItems.register(modEventBus);
        FTCreativeModeTabs.register(modEventBus);
        FTComponents.register(modEventBus);

        FTEnchantmentEffects.register(modEventBus);
        SpearAttachments.register(modEventBus);

        FTRecipes.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, FTConfig.SPEC);

        NeoForge.EVENT_BUS.register(this);
    }
}
