package cn.qihuang02.fantasytools.effect;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.effect.custom.StasisEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FTEffect {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, FantasyTools.MODID);

    public static final Holder<MobEffect> STASIS_EFFECT =
            MOB_EFFECTS.register("stasis", () -> new StasisEffect(MobEffectCategory.BENEFICIAL, 16755200));

    public static void register(IEventBus bus) {
        MOB_EFFECTS.register(bus);
    }
}
