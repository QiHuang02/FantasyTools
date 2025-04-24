package cn.qihuang02.fantasytools.network.packet;

import cn.qihuang02.fantasytools.FantasyTools;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PierceTriggerEffectPacket(int targetEntityId, int stackCount) implements CustomPacketPayload {
    public static final Type<PierceTriggerEffectPacket> TYPE = new Type<>(FantasyTools.getRL("pierce_trigger_effect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PierceTriggerEffectPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            PierceTriggerEffectPacket::targetEntityId,
            ByteBufCodecs.VAR_INT,
            PierceTriggerEffectPacket::stackCount,
            PierceTriggerEffectPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
