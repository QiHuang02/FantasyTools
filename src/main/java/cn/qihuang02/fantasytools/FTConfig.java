package cn.qihuang02.fantasytools;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = FantasyTools.MODID, bus = EventBusSubscriber.Bus.MOD)
public class FTConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    static final ModConfigSpec SPEC = BUILDER.build();
    private static final ModConfigSpec.IntValue ZHONGYA_STASIS_DURATION_TICKS =
            BUILDER.comment("The duration in ticks for the Stasis effect applied by Zhongya's Hourglass.")
                    .defineInRange("stasisDurationTicks", 100, 20, Integer.MAX_VALUE);
    private static final ModConfigSpec.IntValue ZHONGYA_COOLDOWN_TICKS =
            BUILDER.comment("The cooldown in ticks for Zhongya's Hourglass after use.")
                    .defineInRange("cooldownTicks", 600, 0, Integer.MAX_VALUE);
    public static int stasisDurationTicks;
    public static int cooldownTicks;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        stasisDurationTicks = ZHONGYA_STASIS_DURATION_TICKS.get();
        cooldownTicks = ZHONGYA_COOLDOWN_TICKS.get();
    }
}
