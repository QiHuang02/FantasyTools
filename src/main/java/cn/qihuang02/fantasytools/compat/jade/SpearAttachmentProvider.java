package cn.qihuang02.fantasytools.compat.jade;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.attachment.SpearAttachment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum SpearAttachmentProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FantasyTools.MODID, "spear_data");

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        CompoundTag serverData = accessor.getServerData();
        if (serverData.contains("SpearCount")) {
            int count = serverData.getInt("SpearCount");
            if (count > 0) {
                tooltip.add(Component.translatable("tooltip.jade.spear.count", count));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ID;
    }

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        if (accessor.getEntity() instanceof LivingEntity living &&
                accessor.getPlayer() instanceof ServerPlayer serverPlayer) {

            living.getData(SpearAttachment.SPEARS).entrySet().stream()
                    .filter(entry -> entry.getKey().equals(serverPlayer.getUUID()))
                    .findFirst()
                    .ifPresent(entry -> {
                        data.putInt("SpearCount", entry.getValue().count());
                    });
        }
    }
}