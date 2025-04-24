package cn.qihuang02.fantasytools.event;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.FTItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = FantasyTools.MODID, bus = EventBusSubscriber.Bus.GAME)
public class BambooCopterHandler {

    private static final ResourceLocation BAMBOO_COPTER_FLIGHT_MODIFIER_ID = FantasyTools.getRL("bamboo_copter_flight");
    private static final AttributeModifier BAMBOO_COPTER_FLIGHT_MODIFIER = new AttributeModifier(
            BAMBOO_COPTER_FLIGHT_MODIFIER_ID,
            1.0,
            AttributeModifier.Operation.ADD_VALUE
    );

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        AttributeInstance flightAttributeInstance = serverPlayer.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);

        if (flightAttributeInstance == null) {
            FantasyTools.LOGGER.warn("Player {} is missing the CREATIVE_FLIGHT attribute instance!", serverPlayer.getName().getString());
            return;
        }

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        boolean wearingCopter = !helmet.isEmpty() && helmet.is(FTItems.BAMBOO_COPTER.get());

        if (player.isCreative() || player.isSpectator()) {
            if (flightAttributeInstance.hasModifier(BAMBOO_COPTER_FLIGHT_MODIFIER_ID)) {
                flightAttributeInstance.removeModifier(BAMBOO_COPTER_FLIGHT_MODIFIER_ID);
                FantasyTools.LOGGER.debug("Removed Bamboo Copter flight modifier from creative/spectator player {}", player.getName().getString());
                if (player.getAbilities().flying && !player.mayFly()) {
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                }
            }
            return;
        }

        boolean modifierWasPresent = flightAttributeInstance.hasModifier(BAMBOO_COPTER_FLIGHT_MODIFIER_ID);

        if (wearingCopter) {
            if (!modifierWasPresent) {
                flightAttributeInstance.addOrUpdateTransientModifier(BAMBOO_COPTER_FLIGHT_MODIFIER);
                FantasyTools.LOGGER.debug("Applied flight modifier to player {} via Bamboo Copter", player.getName().getString());
            }
        } else {
            if (modifierWasPresent) {
                flightAttributeInstance.removeModifier(BAMBOO_COPTER_FLIGHT_MODIFIER_ID);
                FantasyTools.LOGGER.debug("Removed flight modifier from player {} as Bamboo Copter removed/not equipped", player.getName().getString());

                if (player.getAbilities().flying && !player.mayFly()) {
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                    FantasyTools.LOGGER.debug("Stopped player {} from flying", player.getName().getString());
                }
            }
        }
    }
}
