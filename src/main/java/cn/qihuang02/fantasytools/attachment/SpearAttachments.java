package cn.qihuang02.fantasytools.attachment;

import cn.qihuang02.fantasytools.FantasyTools;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class SpearAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, FantasyTools.MODID);
    public static final Supplier<AttachmentType<Map<UUID, SpearData>>> SPEARS =
            ATTACHMENT_TYPES.register(
                    "spears",
                    () -> AttachmentType
                            .<Map<UUID, SpearData>>builder(() -> new HashMap<>())
                            .serialize(Codec.unboundedMap(UUIDUtil.STRING_CODEC, SpearData.RECORD_CODEC))
                            .build()
            );

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }

    public record SpearData(int count, long lastAttackTick) {
        public static final Codec<SpearData> RECORD_CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.INT.fieldOf("count").forGetter(SpearData::count),
                        Codec.LONG.fieldOf("last_attack_tick").forGetter(SpearData::lastAttackTick)
                ).apply(instance, SpearData::new));
    }
}
