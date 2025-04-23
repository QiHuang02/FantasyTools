package cn.qihuang02.fantasytools.network.packet;

import cn.qihuang02.fantasytools.FantasyTools;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SyncPocketPagePacket(int newPage) implements CustomPacketPayload {
    public static final Type<SyncPocketPagePacket> TYPE = new Type<>(FantasyTools.getRL("sync_pocket_page"));

    public static final StreamCodec<FriendlyByteBuf, SyncPocketPagePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SyncPocketPagePacket::newPage,
            SyncPocketPagePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
