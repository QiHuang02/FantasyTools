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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Zhongya extends Item {
    private static final int STASIS_DURATION_TICKS = 100;
    private static final int COOLDOWN_TICK = 600;

    public Zhongya(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.valueOf("FANTASYTOOLS_LEGENDARY")));
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        Optional<UUID> optionalOwnerUUID = Optional.ofNullable(itemStack.get(FTComponents.OWNER_UUID.get()));
        UUID ownerUUID = player.getUUID();

        if (optionalOwnerUUID.isEmpty()) {
            itemStack.set(FTComponents.OWNER_UUID.get(), ownerUUID);
            player.displayClientMessage(Component.translatable("item.fantasytools.zhongya.bound"), true);
            return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
        }

        if (!Objects.equals(optionalOwnerUUID.get(), ownerUUID)) {
            player.displayClientMessage(Component.translatable("item.fantasytools.zhongya.not_owner"), true);
            return InteractionResultHolder.fail(itemStack);
        }

        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(FTEffect.STASIS_EFFECT, STASIS_DURATION_TICKS, 4, false, true));
            player.displayClientMessage(Component.translatable("item.fantasytools.zhongya.activated"), true);
            player.getCooldowns().addCooldown(this, COOLDOWN_TICK);
            return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
        }

        return InteractionResultHolder.fail(itemStack);
    }

//    @Override
//    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
//        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
//
//        Optional<UUID> optionalOwnerUUID = Optional.ofNullable(stack.get(FTComponents.OWNER_UUID.get()));
//        if (optionalOwnerUUID.isPresent()) {
//            UUID ownerUUID = optionalOwnerUUID.get();
//        }
//    }
}
