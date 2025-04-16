package cn.qihuang02.fantasytools.network.server;

import cn.qihuang02.fantasytools.FTConfig;
import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.component.FTComponents;
import cn.qihuang02.fantasytools.effect.FTEffect;
import cn.qihuang02.fantasytools.network.packet.ACTZYPacket;
import cn.qihuang02.fantasytools.util.HourglassUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public class ServerPayloadHandlers {
    private static final String MSG_NOT_OWNER_KEY = "item.fantasytools.zhongya.not_owner";

    /**
     * 处理 ACTZYPacket 数据包的核心逻辑。
     *
     * @param packet  收到的数据包实例
     * @param context 数据包的上下文，包含发送者等信息
     */
    public static void handleACTZY(final ACTZYPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                ServerPlayer player = getServerPlayer(context);
                if (player == null || !packet.isValid()) {
                    FantasyTools.LOGGER.warn("Invalid ACTZY packet or context Player is empty: {}", packet);
                    return;
                }

                Item hourglassItemFromPacket = packet.stack().getItem();
                ItemStack hourglassInstance = HourglassUtils.findFirstPresentHourglass(player);

                if (hourglassInstance.isEmpty()) {
                    FantasyTools.LOGGER.warn("Server: Player {} sent an ACTZY packet, but no item was found in its handheld or Curios {}",
                            player.getName().getString(), hourglassItemFromPacket.getDescriptionId());
                    return;
                }
                if (!hourglassInstance.is(hourglassItemFromPacket)) {
                    FantasyTools.LOGGER.error("Server: The item found {} does not match the item {} in the packet! Player {}",
                            hourglassInstance.getDescriptionId(), hourglassItemFromPacket.getDescriptionId(), player.getName().getString());
                    return;
                }

                UUID ownerUUID = player.getUUID();
                UUID existingOwner = hourglassInstance.get(FTComponents.OWNER);

                if (existingOwner == null) {
                    hourglassInstance.set(FTComponents.OWNER, ownerUUID);
                    FantasyTools.LOGGER.debug("Set owner {} for item {}", ownerUUID, hourglassInstance.getDescriptionId());
                } else if (!existingOwner.equals(ownerUUID)) {
                    handleNotOwner(player, hourglassItemFromPacket);
                    return;
                }

                if (player.getCooldowns().isOnCooldown(hourglassItemFromPacket)) {
                    FantasyTools.LOGGER.debug("Player {} tries to use item {}, but the item is in cooldown", player.getName().getString(), hourglassItemFromPacket.getDescriptionId());
                    return;
                }

                applyStasisEffect(player);

                setGlobalCooldown(player, hourglassItemFromPacket);

            } catch (Exception e) {
                FantasyTools.LOGGER.error("An error occurred while processing ACTZY packets, player: {}",
                        context.player() != null ? context.player().getName().getString() : "[未知]", e);
            }
        });
    }

    /**
     * 从上下文中安全地获取 ServerPlayer。
     *
     * @param context 数据包上下文
     * @return ServerPlayer 实例，如果上下文中的玩家不是 ServerPlayer 则返回 null
     */
    private static ServerPlayer getServerPlayer(IPayloadContext context) {
        return context.player() instanceof ServerPlayer sp ? sp : null;
    }

    /**
     * 处理玩家尝试使用非自己所有的沙漏的情况。
     *
     * @param player 尝试使用的玩家
     * @param item   沙漏的物品类型 (Item)
     */
    private static void handleNotOwner(ServerPlayer player, Item item) {
        player.getCooldowns().addCooldown(item, FTConfig.COOLDOWN_TICKS.get());
        player.displayClientMessage(Component.translatable(MSG_NOT_OWNER_KEY), true);
        FantasyTools.LOGGER.warn("Player {} Try to use an hourglass that does not belong to him/her ({})", player.getName().getString(), item.getDescriptionId());
    }


    /**
     * 向玩家施加静滞效果。
     *
     * @param player 目标玩家
     */
    private static void applyStasisEffect(ServerPlayer player) {
        int duration = FTConfig.STASIS_DURATION_TICKS.get();
        int amplifier = 4;
        boolean ambient = false;
        boolean showParticles = true;
        boolean showIcon = true;

        MobEffectInstance effectInstance = new MobEffectInstance(
                FTEffect.STASIS_EFFECT,
                duration,
                amplifier,
                ambient,
                showParticles,
                showIcon
        );

        player.addEffect(effectInstance);
        player.fallDistance = 0.0F;
        FantasyTools.LOGGER.debug("Has applied static effect to player {} for {} ticks", player.getName().getString(), duration);
    }

    /**
     * 为指定的物品类型在玩家身上设置全局冷却。
     *
     * @param player         目标玩家
     * @param itemToCooldown 需要设置冷却的物品类型 (Item)
     */
    private static void setGlobalCooldown(ServerPlayer player, Item itemToCooldown) {
        int cooldownTicks = FTConfig.COOLDOWN_TICKS.get();
        if (cooldownTicks > 0) {
            player.getCooldowns().addCooldown(itemToCooldown, cooldownTicks);
            FantasyTools.LOGGER.debug("Cooldown {} ticks have been set for the item type {} of player {}",
                    player.getName().getString(), itemToCooldown.getDescriptionId(), cooldownTicks);
        }
    }
}
