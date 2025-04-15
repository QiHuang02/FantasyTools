package cn.qihuang02.fantasytools.network.packet;

import cn.qihuang02.fantasytools.FantasyTools;
import cn.qihuang02.fantasytools.item.custom.Hourglass;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * 一个自定义网络数据包，用于从客户端向服务器发送激活沙漏的请求。
 * 使用 Java Record 类型，它非常适合表示这种不可变的数据载体。
 *
 * @param stack 发送时客户端持有的相关 ItemStack (通常是触发操作的沙漏)
 */
public record ACTZYPacket(ItemStack stack) implements CustomPacketPayload {

    public static final Type<ACTZYPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FantasyTools.MODID, "actzy"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ACTZYPacket> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC,
            ACTZYPacket::stack,
            ACTZYPacket::new
    );

    /**
     * Record 的规范构造函数。
     * 在这里进行参数验证和处理，例如创建防御性副本。
     */
    public ACTZYPacket(ItemStack stack) {
        this.stack = Objects.requireNonNullElse(stack, ItemStack.EMPTY).copy();
    }

    /**
     * 返回此数据包的类型。
     *
     * @return 数据包类型实例
     */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 在服务器端进行的基本验证。
     * 检查包含的 ItemStack 是否非空且确实是一个沙漏物品。
     * @return 如果数据包内容有效则返回 true，否则返回 false。
     */
    public boolean isValid() {
        return !this.stack.isEmpty() && this.stack.getItem() instanceof Hourglass;
    }

    /**
     * 提供一个易于阅读的字符串表示形式，用于日志记录和调试。
     * @return 数据包内容的字符串描述
     */
    @Override
    public String toString() {
        return "ACTZYPacket[" +
                "Item=" + (this.stack.isEmpty() ? "EMPTY" : this.stack.getItem().getDescriptionId()) +
                ", Count=" + this.stack.getCount() +
                "]";
    }
}