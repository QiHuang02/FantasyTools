package cn.qihuang02.fantasytools.network.client;

import cn.qihuang02.fantasytools.menu.PocketMenu;
import cn.qihuang02.fantasytools.network.packet.PierceStackEffectPacket;
import cn.qihuang02.fantasytools.network.packet.PierceTriggerEffectPacket;
import cn.qihuang02.fantasytools.network.packet.SyncPocketPagePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandlers {
    public static void handlePierceStackEffect(final PierceStackEffectPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }

            Entity targetEntity = level.getEntity(packet.targetEntityId());
            if (targetEntity != null) {
                for (int i = 0; i < 3; ++i) {
                    level.addParticle(ParticleTypes.ENCHANTED_HIT,
                            targetEntity.getRandomX(0.5),
                            targetEntity.getRandomY(),
                            targetEntity.getRandomZ(0.5),
                            (level.random.nextDouble() - 0.5) * 0.1,
                            level.random.nextDouble() * 0.1 + 0.05,
                            (level.random.nextDouble() - 0.5) * 0.1);
                }
                level.playLocalSound(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ(),
                        SoundEvents.ARROW_HIT_PLAYER,
                        net.minecraft.sounds.SoundSource.NEUTRAL,
                        2.0F,
                        1.5F + level.random.nextFloat() * 0.5F,
                        true);
            }
        });
    }

    public static void handlePierceTriggerEffect(final PierceTriggerEffectPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level == null) {
                return;
            }

            Entity targetEntity = level.getEntity(packet.targetEntityId());
            if (targetEntity != null) {
                int particleCount = Math.min(10 + packet.stackCount(), 40);
                for (int i = 0; i < particleCount; ++i) {
                    level.addParticle(ParticleTypes.CRIT,
                            targetEntity.getRandomX(0.7),
                            targetEntity.getRandomY(),
                            targetEntity.getRandomZ(0.7),
                            (level.random.nextDouble() - 0.5) * 0.5,
                            level.random.nextDouble() * 0.2 + 0.1,
                            (level.random.nextDouble() - 0.5) * 0.5);
                }
                if (level.random.nextInt(2) == 0) {
                    level.addParticle(ParticleTypes.SWEEP_ATTACK,
                            targetEntity.getX(), targetEntity.getY(0.6), targetEntity.getZ(),
                            0, 0, 0);
                }

                level.playLocalSound(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ(),
                        SoundEvents.PLAYER_ATTACK_CRIT,
                        net.minecraft.sounds.SoundSource.PLAYERS,
                        2.0F,
                        1.0F + level.random.nextFloat() * 0.2F,
                        true);
            }
        });
    }

    public static void handleSyncPocketPage(SyncPocketPagePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                AbstractContainerMenu currentMenu = mc.player.containerMenu;
                if (currentMenu instanceof PocketMenu pocketMenu) {
                    // 调用修改后的同步方法，传入页面、canGoNext 和 maxPages 状态
                    pocketMenu.syncClientState(packet.newPage(), packet.canGoNext(), packet.maxPages()); // 添加 packet.maxPages()
                }
            }
        });
    }
}
