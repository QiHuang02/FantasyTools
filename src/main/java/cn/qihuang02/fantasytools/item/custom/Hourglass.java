package cn.qihuang02.fantasytools.item.custom;

import cn.qihuang02.fantasytools.component.FTComponents;
import cn.qihuang02.fantasytools.event.client.KeyMappings;
import cn.qihuang02.fantasytools.network.packet.ACTZYPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Hourglass extends Item {
    public Hourglass(@NotNull Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.valueOf("FANTASYTOOLS_LEGENDARY")));
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level world, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);

        if (!world.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(item, false);
        } else {
            PacketDistributor.sendToServer(new ACTZYPacket(item));
            return InteractionResultHolder.sidedSuccess(item, true);
        }
    }

    @Override
    public void appendHoverText(
            @NotNull ItemStack stack,
            @NotNull TooltipContext context,
            @NotNull List<Component> tooltipComponents,
            @NotNull TooltipFlag tooltipFlag
    ) {
        Optional<UUID> optionalUUID = Optional.ofNullable(stack.get(FTComponents.OWNER));

        String playerName = optionalUUID.map(uuid -> {
            Player player = Objects.requireNonNull(context.level()).getPlayerByUUID(uuid);
            return player != null ? player.getName().getString() : "Unknown";
        }).orElse("Unknown");

        String key = KeyMappings.ACTIVATE_ZHONGYA_KEY.getKey().getDisplayName().getString();

        tooltipComponents.add(1, Component.translatable("item.fantasytools.zhongya.owner", playerName));
        tooltipComponents.add(2, Component.translatable("item.fantasytools.zhongya.key", key));
    }
}
