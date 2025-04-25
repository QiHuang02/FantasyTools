package cn.qihuang02.fantasytools.network.packet;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.custom.Hourglass;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public record ACTZYPacket() implements CustomPacketPayload {

    public static final Type<ACTZYPacket> TYPE = new Type<>(FantasyTools.getRL("actzy"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ACTZYPacket> STREAM_CODEC = StreamCodec.unit(
            new ACTZYPacket()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}