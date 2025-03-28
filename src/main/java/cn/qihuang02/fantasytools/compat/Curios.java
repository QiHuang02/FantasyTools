package cn.qihuang02.fantasytools.compat;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.Optional;

public class Curios {
    private static Optional<Map<String, ICurioStacksHandler>> resolveCuriosMap(LivingEntity entity) {
        return Optional.ofNullable(entity.getCapability(CuriosCapability.INVENTORY).getCurios()); /* .map(ICuriosItemHandler::getCurios); */
    };
    
    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(Curios::registerCap);
    }

    private static void registerCap(final RegisterCapabilitiesEvent event) {
        event.registerItem(
                CuriosCapability.ITEM,
                (stack, context) -> (ICurio) () -> stack
        );
    }
}
