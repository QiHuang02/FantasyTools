package cn.qihuang02.fantasytools.util;

import cn.qihuang02.fantasytools.effect.FTEffect;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StasisUtil {
    private static final Map<UUID, Boolean> STASIS_CACHE = new HashMap<>();
    private static final Map<UUID, Long> LAST_CHECK_TIME = new HashMap<>();
    private static final long CHECK_INTERVAL = 5L;

    public static boolean isStasis(LivingEntity entity) {
        if (entity == null) return false;

        UUID uuid = entity.getUUID();
        long currentTime = entity.level().getGameTime();

        if (!LAST_CHECK_TIME.containsKey(uuid) || currentTime - LAST_CHECK_TIME.get(uuid) > CHECK_INTERVAL) {
            boolean hasEffect = entity.hasEffect(FTEffect.STASIS_EFFECT);

            STASIS_CACHE.put(uuid, hasEffect);
            LAST_CHECK_TIME.put(uuid, currentTime);
        }

        return STASIS_CACHE.getOrDefault(uuid, false);
    }
}
