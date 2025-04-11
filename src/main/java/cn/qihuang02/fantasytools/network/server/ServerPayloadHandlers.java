package cn.qihuang02.fantasytools.network.server;

import cn.qihuang02.fantasytools.FTConfig;
import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.component.FTComponents;
import cn.qihuang02.fantasytools.effect.FTEffect;
import cn.qihuang02.fantasytools.item.custom.Hourglass;
import cn.qihuang02.fantasytools.network.packet.ACTZYPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ServerPayloadHandlers {
    public static void handleACTZY(final ACTZYPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                if (!validatePayload(packet, context)) return;

                final ServerPlayer player = (ServerPlayer) context.player();
                final ItemStack hourglass = packet.stack();

                if (!player.server.isSameThread()) {
                    FantasyTools.LOGGER.warn("Packet handled on wrong thread");
                    return;
                }

                Optional<UUID> optionalUUID = Optional.ofNullable(hourglass.get(FTComponents.OWNER));
                UUID ownerUUID = player.getUUID();

                if (optionalUUID.isEmpty()) {
                    hourglass.set(FTComponents.OWNER, ownerUUID);
                } else if (!optionalUUID.get().equals(ownerUUID)) {
                    player.getCooldowns().addCooldown(hourglass.getItem(), FTConfig.COOLDOWN_TICKS.get());
                    player.displayClientMessage(Component.translatable("item.fantasytools.zhongya.not_owner"), true);
                    return;
                }

                if (isOnCooldown(player, hourglass)) return;
                if (!hasHourglass(player, hourglass)) return;

                applyStasisEffect(player);
                setCooldown(player);
            } catch (Exception e) {
                FantasyTools.LOGGER.error("Error handling ACTZY packet", e);
            }
        });
    }

    private static void setCooldown(ServerPlayer player) {
        int cooldown = FTConfig.COOLDOWN_TICKS.get();

        player.getInventory().items.forEach(stack -> {
            if (stack.getItem() instanceof Hourglass) {
                player.getCooldowns().addCooldown(stack.getItem(), cooldown);
            }
        });

        CuriosApi.getCuriosInventory(player).ifPresent(inv -> {
            inv.findCurios(stack -> stack.getItem() instanceof Hourglass)
                    .forEach(slot ->
                            player.getCooldowns().addCooldown(slot.stack().getItem(), cooldown)
                    );
        });
    }

    private static void applyStasisEffect(ServerPlayer player) {
        int duration = FTConfig.STASIS_DURATION_TICKS.get();
        MobEffectInstance effect = new MobEffectInstance(FTEffect.STASIS_EFFECT, duration, 4, false, true, true);

        player.addEffect(effect);
        player.fallDistance = 0.0F;
    }

    private static boolean hasHourglass(ServerPlayer player, ItemStack hourglass) {
        boolean inInventory = player.getInventory().contains(hourglass);

        boolean inCurios = CuriosApi.getCuriosInventory(player)
                .map(inv -> {
                    List<SlotResult> results = inv.findCurios(stack ->
                            ItemStack.isSameItem(stack, hourglass)
                    );
                    return !results.isEmpty();
                })
                .orElse(false);

        return inInventory || inCurios;
    }

    private static boolean isOnCooldown(ServerPlayer player, ItemStack hourglass) {
        return player.getCooldowns().isOnCooldown(hourglass.getItem());
    }

    private static boolean validatePayload(ACTZYPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer)) return false;
        return packet.isValid();
    }
}
