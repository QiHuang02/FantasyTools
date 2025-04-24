package cn.qihuang02.fantasytools.network.packet;

import cn.qihuang02.fantasytools.FantasyTools;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record ChangePocketPagePacket(int requestedPage) implements CustomPacketPayload {
    public static final Type<ChangePocketPagePacket> TYPE = new Type<>(FantasyTools.getRL("change_pocket_page"));

    public static final StreamCodec<FriendlyByteBuf, ChangePocketPagePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ChangePocketPagePacket::requestedPage,
            ChangePocketPagePacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
