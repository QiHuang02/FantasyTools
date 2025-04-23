package cn.qihuang02.fantasytools.item.custom;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.component.FTComponents;
import cn.qihuang02.fantasytools.network.server.ServerPayloadHandlers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FourDimensionalPocket extends Item {
    public FourDimensionalPocket(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.valueOf("FANTASYTOOLS_LEGENDARY")));
    }

    public static void ensurePocketUUID(ItemStack stack) {
        if (stack.getItem() instanceof FourDimensionalPocket && !stack.has(FTComponents.POCKET_UUID.get())) {
            UUID newUuid = UUID.randomUUID();
            stack.set(FTComponents.POCKET_UUID.get(), newUuid);
            FantasyTools.LOGGER.debug("Assigned new UUID {} to FourDimensionalPocket", newUuid);
        }
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ensurePocketUUID(stack);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            UUID pocketId = stack.get(FTComponents.POCKET_UUID.get());
            if (pocketId != null) {
                MenuProvider menuProvider = ServerPayloadHandlers.createPocketMenuProvider(stack, pocketId);
                serverPlayer.openMenu(menuProvider, buf -> {
                    buf.writeUUID(pocketId);
                    buf.writeVarInt(0);
                });
            } else {
                FantasyTools.LOGGER.error("Pocket item {} used by {} is missing POCKET_UUID component after ensuring!", stack, player.getName().getString());
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
