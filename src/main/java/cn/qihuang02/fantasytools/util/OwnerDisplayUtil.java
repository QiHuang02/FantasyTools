package cn.qihuang02.fantasytools.util;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class OwnerDisplayUtil {
    private static final Map<UUID, String> CLIENT_UUID_NAME_MAP = new HashMap<UUID, String>();

    @Nullable
    public static String getPlayerName(Level level, UUID uuid) {
        if (level.isClientSide) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.getUUID().equals(uuid)) {
                return mc.player.getGameProfile().getName();
            }
            return CLIENT_UUID_NAME_MAP.getOrDefault(uuid, mc.player.getGameProfile().getName());
        } else {
            ServerPlayer player = Objects.requireNonNull(level.getServer()).getPlayerList().getPlayer(uuid);
            return player != null ? player.getGameProfile().getName() : null;
        }
    }

    public static void cachePlayerName(UUID uuid, String name) {
        CLIENT_UUID_NAME_MAP.put(uuid, name);
    }
}
