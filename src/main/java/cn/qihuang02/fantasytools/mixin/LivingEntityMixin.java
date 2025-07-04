package cn.qihuang02.fantasytools.mixin;

import cn.qihuang02.fantasytools.effect.FTEffect;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(
            method = "travel",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onTravel(Vec3 travelVector, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.hasEffect(FTEffect.STASIS_EFFECT)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "hurt",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.hasEffect(FTEffect.STASIS_EFFECT)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "onItemPickup",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onItemPickup(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.hasEffect(FTEffect.STASIS_EFFECT)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAddEffect(MobEffectInstance effectInstance, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.hasEffect(FTEffect.STASIS_EFFECT)) {
            cir.setReturnValue(true);
        }
    }
}
