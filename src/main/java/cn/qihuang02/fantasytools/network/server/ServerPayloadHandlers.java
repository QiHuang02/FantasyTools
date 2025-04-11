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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ServerPayloadHandlers {
    public static void handleACTZY(final ACTZYPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                if (!validatePayload(packet, context)) return;

                final ServerPlayer player = (ServerPlayer) context.player();
                if (!player.server.isSameThread()) {
                    FantasyTools.LOGGER.warn("Packet handled on wrong thread");
                    return;
                }

                ItemStack hourglass = findActualHourglass(player, packet.stack());
                if (hourglass == null) {
                    FantasyTools.LOGGER.warn("Hourglass not found");
                    return;
                }

                UUID ownerUUID = player.getUUID();
                UUID existingOwner = hourglass.get(FTComponents.OWNER);

                if (existingOwner == null) {
                    hourglass.set(FTComponents.OWNER, ownerUUID);
                    updateCuriosSlots(player, hourglass, ownerUUID);
                } else if (!existingOwner.equals(ownerUUID)) {
                    handleNotOwner(player, hourglass);
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

    private static void handleNotOwner(ServerPlayer player, ItemStack hourglass) {
        player.getCooldowns().addCooldown(hourglass.getItem(), FTConfig.COOLDOWN_TICKS.get());
        player.displayClientMessage(Component.translatable("item.fantasytools.zhongya.not_owner"), true);
    }

    private static void updateCuriosSlots(ServerPlayer player, ItemStack hourglass, UUID ownerUUID) {
        if (isInCuriosSlot(player, hourglass)) {
            CuriosApi.getCuriosInventory(player).ifPresent(inv ->
                    inv.findCurios(stack -> stack.getItem() == hourglass.getItem())
                            .forEach(slot -> slot.stack().set(FTComponents.OWNER, ownerUUID)));
        }
    }

    private static boolean isInCuriosSlot(ServerPlayer player, ItemStack targetStack) {
        return CuriosApi.getCuriosInventory(player)
                .map(inv -> !inv.findCurios(stack ->
                        ItemStack.isSameItemSameComponents(stack, targetStack)).isEmpty())
                .orElse(false);
    }

    private static ItemStack findActualHourglass(ServerPlayer player, ItemStack reference) {
        if (isSameHourglass(player.getMainHandItem(), reference)) {
            return player.getMainHandItem();
        }
        if (isSameHourglass(player.getOffhandItem(), reference)) {
            return player.getOffhandItem();
        }

        List<SlotResult> curios = CuriosApi.getCuriosInventory(player)
                .map(inv -> inv.findCurios(stack -> stack.getItem() == reference.getItem()))
                .orElse(Collections.emptyList());

        return curios.isEmpty() ? null : curios.get(0).stack();
    }

    private static boolean isSameHourglass(ItemStack a, ItemStack b) {
        return a.getItem() instanceof Hourglass && b.getItem() instanceof Hourglass && ItemStack.isSameItem(a, b);
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
        boolean inHand = player.getMainHandItem().getItem() instanceof Hourglass ||
                player.getOffhandItem().getItem() instanceof Hourglass;

        boolean inCurios = CuriosApi.getCuriosInventory(player)
                .map(inv -> {
                    List<SlotResult> results = inv.findCurios(stack ->
                            stack.getItem() == hourglass.getItem()
                    );
                    return !results.isEmpty();
                })
                .orElse(false);

        return inHand || inCurios;
    }

    private static boolean isOnCooldown(ServerPlayer player, ItemStack hourglass) {
        return player.getCooldowns().isOnCooldown(hourglass.getItem());
    }

    private static boolean validatePayload(ACTZYPacket packet, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer)) return false;
        return packet.isValid();
    }
}
