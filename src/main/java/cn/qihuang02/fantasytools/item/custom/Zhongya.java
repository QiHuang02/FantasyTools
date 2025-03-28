package cn.qihuang02.fantasytools.item.custom;

import cn.qihuang02.fantasytools.component.FTComponents;
import cn.qihuang02.fantasytools.effect.FTEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class Zhongya extends Item{
    private static final int STASIS_DURATION_TICKS = 100;
    private static final int COOLDOWN_TICK = 600;

    public Zhongya(@NotNull Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.valueOf("FANTASYTOOLS_LEGENDARY")));
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);

        Optional<String> optionalOwnerName = Optional.ofNullable(item.get(FTComponents.OWNER));
        String OwnerName = player.getName().getString();

        if (optionalOwnerName.isEmpty() || player instanceof ServerPlayer) {
            item.set(FTComponents.OWNER, OwnerName);
            player.addEffect(new MobEffectInstance(FTEffect.STASIS_EFFECT, STASIS_DURATION_TICKS, 4, false, true));
            player.getCooldowns().addCooldown(this, COOLDOWN_TICK);
            player.fallDistance = 0.0F;
            return InteractionResultHolder.sidedSuccess(item, world.isClientSide());
        }

        if (!optionalOwnerName.get().equals(OwnerName)) {
            player.getCooldowns().addCooldown(this, COOLDOWN_TICK);
            player.displayClientMessage(Component.translatable("item.fantasytools.zhongya.not_owner"), true);
            return InteractionResultHolder.fail(item);
        }
        return InteractionResultHolder.fail(item);
    }

    @Override
    public void appendHoverText(
            @NotNull ItemStack stack,
            @NotNull TooltipContext context,
            @NotNull List<Component> tooltipComponents,
            @NotNull TooltipFlag tooltipFlag
    ) {
        Optional<String> optionalOwnerName = Optional.ofNullable(stack.get(FTComponents.OWNER));

        optionalOwnerName.ifPresent(s -> tooltipComponents.addLast(Component.translatable("item.fantasytools.zhongya.owner", s)));

        if (optionalOwnerName.isEmpty()) {
            tooltipComponents.addLast(Component.translatable("item.fantasytools.zhongya.no_owner"));
        }
    }
}
