package cn.qihuang02.fantasytools.network.packet;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.custom.Hourglass;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record ACTZYPacket(ItemStack stack) implements CustomPacketPayload {
    public static final Type<ACTZYPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FantasyTools.MODID, "actzy"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ACTZYPacket> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC,
            ACTZYPacket::stack,
            ACTZYPacket::new
    );

    public ACTZYPacket {
        stack = stack.copy();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public boolean isValid() {
        return !stack.isEmpty() && stack.getItem() instanceof Hourglass;
    }

    @Override
    public String toString() {
        return "ZhongyaActivationPayload[item=" +
                stack.getItem().getDescription().getString() +
                ", count=" + stack.getCount() + "]";
    }
}