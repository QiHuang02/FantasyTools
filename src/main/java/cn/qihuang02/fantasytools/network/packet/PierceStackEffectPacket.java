package cn.qihuang02.fantasytools.network.packet;

import cn.qihuang02.fantasytools.FantasyTools;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PierceStackEffectPacket(int targetEntityId) implements CustomPacketPayload {
    public static final Type<PierceStackEffectPacket> TYPE = new Type<>(FantasyTools.getRL("pierce_stack_effect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PierceStackEffectPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            PierceStackEffectPacket::targetEntityId,
            PierceStackEffectPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
