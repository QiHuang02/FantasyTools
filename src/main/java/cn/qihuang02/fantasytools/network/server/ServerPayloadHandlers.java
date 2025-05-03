package cn.qihuang02.fantasytools.network.server;

import cn.qihuang02.fantasytools.FTConfig;
import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.component.FTComponents;
import cn.qihuang02.fantasytools.menu.data.PocketDataManager;
import cn.qihuang02.fantasytools.menu.data.PocketInventory;
import cn.qihuang02.fantasytools.effect.FTEffect;
import cn.qihuang02.fantasytools.item.custom.FourDimensionalPocket;
import cn.qihuang02.fantasytools.menu.PocketMenu;
import cn.qihuang02.fantasytools.network.packet.ACTZYPacket;
import cn.qihuang02.fantasytools.network.packet.ChangePocketPagePacket;
import cn.qihuang02.fantasytools.network.packet.OpenPocketPacket;
import cn.qihuang02.fantasytools.network.packet.SyncPocketPagePacket;
import cn.qihuang02.fantasytools.util.HourglassUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

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
                if (player == null) {
                    FantasyTools.LOGGER.warn("Invalid ACTZY packet or context Player is empty: {}", packet);
                    return;
                }

                ItemStack hourglassInstance = HourglassUtils.findFirstPresentHourglass(player);

                if (hourglassInstance.isEmpty()) {
                    FantasyTools.LOGGER.warn("Server: Player {} sent an ACTZY packet, but no hourglass was found.",
                            player.getName().getString());
                    return;
                }

                Item hourglassItem = hourglassInstance.getItem();

                UUID ownerUUID = player.getUUID();
                UUID existingOwner = hourglassInstance.get(FTComponents.OWNER);

                if (existingOwner == null) {
                    hourglassInstance.set(FTComponents.OWNER, ownerUUID);
                    FantasyTools.LOGGER.debug("Set owner {} for item {}", ownerUUID, hourglassInstance.getDescriptionId());
                } else if (!existingOwner.equals(ownerUUID)) {
                    handleNotOwner(player, hourglassItem);
                    return;
                }

                if (player.getCooldowns().isOnCooldown(hourglassItem)) {
                    FantasyTools.LOGGER.debug("Player {} tries to use item {}, but the item is in cooldown", player.getName().getString(), hourglassItem.getDescriptionId());
                    return;
                }

                applyStasisEffect(player);

                setGlobalCooldown(player, hourglassItem);

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

    /**
     * Handles the request from the client to open the Pocket GUI. (Original Version)
     */
    public static void handleOpenPocket(final OpenPocketPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = getServerPlayer(context);
            if (player == null) return;

            InteractionHand hand = packet.hand();
            ItemStack pocketStack = player.getItemInHand(hand);

            if (!(pocketStack.getItem() instanceof FourDimensionalPocket)) {
                FantasyTools.LOGGER.warn("Player {} tried to open pocket with non-pocket item in hand {}: {}",
                        player.getName().getString(), hand, pocketStack.getItem().getDescriptionId());
                return;
            }

            FourDimensionalPocket.ensurePocketUUID(pocketStack);
            UUID pocketId = pocketStack.get(FTComponents.POCKET_UUID.get());

            if (pocketId == null) {
                FantasyTools.LOGGER.error("Pocket item {} used by {} is missing POCKET_UUID component after ensuring!", pocketStack, player.getName().getString());
                return;
            }

            MenuProvider menuProvider = createPocketMenuProvider(pocketStack, pocketId);
            player.openMenu(menuProvider, buf -> {
                buf.writeUUID(pocketId);
                buf.writeVarInt(0);
            });

            if (player.containerMenu instanceof PocketMenu pocketMenu && player.level() instanceof ServerLevel serverLevel) {
                PocketInventory realInventory = PocketDataManager.get(serverLevel).getOrCreateInventory(pocketId);

                int initialPage = 0;
                int currentMaxPages = realInventory.getMaxPages();
                boolean canAddPage = realInventory.canAddPage();
                boolean initialCanGoNext = canAddPage || (initialPage + 1) < currentMaxPages;

                SyncPocketPagePacket initialSyncPacket = new SyncPocketPagePacket(initialPage, initialCanGoNext, currentMaxPages);
                PacketDistributor.sendToPlayer(player, initialSyncPacket);

                FantasyTools.LOGGER.debug("Opened pocket {}. Sent initial sync: page={}, canGoNext={}, maxPages={}",
                        pocketId.toString().substring(0, 8), initialPage, initialCanGoNext, currentMaxPages);

            } else {
                FantasyTools.LOGGER.error("Failed to get PocketMenu instance or ServerLevel after opening menu for player {}", player.getName().getString());
            }
        });
    }

    /**
     * Handles the request from the client to change the pocket page. (Original Version)
     */
    public static void handleChangePocketPage(final ChangePocketPagePacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ServerPlayer player = getServerPlayer(context);
            if (player == null) return;

            if (player.containerMenu instanceof PocketMenu pocketMenu) {
                pocketMenu.changePageServer(packet.requestedPage());
            } else {
                FantasyTools.LOGGER.warn("Player {} sent ChangePocketPagePacket but is not in PocketMenu. Current menu: {}",
                        player.getName().getString(), player.containerMenu != null ? player.containerMenu.getType() : "null");
            }
        });
    }

    public static MenuProvider createPocketMenuProvider(ItemStack stack, UUID pocketId) {
        return new MenuProvider() {
            @Override
            public @NotNull Component getDisplayName() {
                return stack.getHoverName();
            }

            @Override
            public @NotNull AbstractContainerMenu createMenu(int containerId, @NotNull Inventory playerInventory, @NotNull Player player) {
                return new PocketMenu(containerId, playerInventory, pocketId, 0); // Always starts at page 0
            }
        };
    }
}
