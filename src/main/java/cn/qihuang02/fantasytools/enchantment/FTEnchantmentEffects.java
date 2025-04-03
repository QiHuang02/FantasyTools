package cn.qihuang02.fantasytools.enchantment;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.enchantment.custom.PierceEnchantmentEffect;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class FTEnchantmentEffects {
    public static final DeferredRegister<MapCodec<? extends EnchantmentEntityEffect>> ENTITY_ENCHANTMENTS_EFFECTS =
            DeferredRegister.create(Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE, FantasyTools.MODID);

    public static final Supplier<MapCodec<? extends EnchantmentEntityEffect>> PIERCE =
            ENTITY_ENCHANTMENTS_EFFECTS.register("pierce", () -> PierceEnchantmentEffect.CODEC);

    public static void register(IEventBus bus) {
        ENTITY_ENCHANTMENTS_EFFECTS.register(bus);
    }
}
