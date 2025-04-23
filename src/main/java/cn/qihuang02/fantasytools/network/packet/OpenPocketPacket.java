package cn.qihuang02.fantasytools.network.packet;

import cn.qihuang02.fantasytools.FantasyTools;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;

public record OpenPocketPacket(InteractionHand hand) implements CustomPacketPayload {
    public static final Type<OpenPocketPacket> TYPE = new Type<>(FantasyTools.getRL("open_pocket"));

    public static final StreamCodec<FriendlyByteBuf, OpenPocketPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull OpenPocketPacket decode(@NotNull FriendlyByteBuf buf) {
            int ordinal = ByteBufCodecs.VAR_INT.decode(buf);
            InteractionHand[] hands = InteractionHand.values();
            if (ordinal < 0 || ordinal >= hands.length) {
                throw new DecoderException("Invalid ordinal for InteractionHand: " + ordinal);
            }
            return new OpenPocketPacket(hands[ordinal]);
        }

        @Override
        public void encode(@NotNull FriendlyByteBuf buf, OpenPocketPacket packet) {
            ByteBufCodecs.VAR_INT.encode(buf, packet.hand().ordinal());
        }
    };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
