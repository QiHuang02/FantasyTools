package cn.qihuang02.fantasytools.util;

import cn.qihuang02.fantasytools.item.FTItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;

public class HourglassUtils {
    private static final Item HOURGLASS = FTItems.ZHONGYAHOURGLASS.get();

    private HourglassUtils() {
    }

    public static ItemStack findFirstPresentHourglass(Player player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }

        ItemStack mainHandStack = player.getMainHandItem();
        if (mainHandStack.is(HOURGLASS)) {
            return mainHandStack;
        }

        ItemStack offHandStack = player.getOffhandItem();
        if (offHandStack.is(HOURGLASS)) {
            return offHandStack;
        }

        Optional<ItemStack> curiosResult = findHourglassInCurios(player);
        return curiosResult.orElse(ItemStack.EMPTY);

    }

    public static ItemStack findFirstEligibleHourglassClient(Player player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }

        if (player.getCooldowns().isOnCooldown(HOURGLASS)) {
            return ItemStack.EMPTY;
        }

        Optional<ItemStack> curiosResult = findHourglassInCurios(player);
        if (curiosResult.isPresent()) {
            return curiosResult.get();
        }

        for (ItemStack inventoryStack : player.getInventory().items) {
            if (inventoryStack.is(HOURGLASS)) {
                return inventoryStack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static Optional<ItemStack> findHourglassInCurios(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(inv -> inv.findFirstCurio(HOURGLASS)) // 查找第一个匹配的 Curio
                .map(SlotResult::stack);
    }
}
