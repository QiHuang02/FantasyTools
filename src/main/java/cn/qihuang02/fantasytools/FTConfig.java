package cn.qihuang02.fantasytools;

import net.neoforged.neoforge.common.ModConfigSpec;

public class FTConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.ConfigValue<Integer> STASIS_DURATION_TICKS;
    public static final ModConfigSpec.ConfigValue<Integer> COOLDOWN_TICKS;

    public static final ModConfigSpec SPEC;

    static {
        BUILDER.comment("FTConfig - Configuration for FantasyTools");
        BUILDER.push("zhongya_hourglass");
        BUILDER.comment("Settings related to Zhongya's Hourglass");
        STASIS_DURATION_TICKS = BUILDER
                .comment("The duration in ticks for the Stasis effect applied by Zhongya's Hourglass.")
                .defineInRange("stasis_duration_ticks",
                        100,
                        20,
                        Integer.MAX_VALUE);
        COOLDOWN_TICKS = BUILDER
                .comment("The cooldown in ticks for Zhongya's Hourglass after use.")
                .defineInRange("cooldown_ticks",
                        600,
                        0,
                        Integer.MAX_VALUE);
        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
