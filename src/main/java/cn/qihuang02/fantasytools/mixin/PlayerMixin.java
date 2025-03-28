package cn.qihuang02.fantasytools.mixin;

import cn.qihuang02.fantasytools.util.StasisUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(
            method = "attack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAttack(Entity target, CallbackInfo ci) {
        Player player = (Player)(Object)this;
        if (StasisUtil.isStasis(player)) {
            ci.cancel();
        }
    }
}
