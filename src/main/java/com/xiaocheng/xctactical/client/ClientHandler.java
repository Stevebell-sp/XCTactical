package com.xiaocheng.xctactical.client;

import com.xiaocheng.xctactical.XCTactical;
import com.xiaocheng.xctactical.net.C2SSilentWalk;
import com.xiaocheng.xctactical.net.XCNet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = XCTactical.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientHandler {

    // 比正常走路(1.0)慢、比蹲行(~0.3)快
    private static final float WALK_FACTOR = 0.55f;

    private static boolean lastSent = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();

        // 1) 鎖碰撞箱顯示為關（F3+B 會被立刻復原）
        if (mc.getEntityRenderDispatcher().shouldRenderHitBoxes()) {
            mc.getEntityRenderDispatcher().setRenderHitBoxes(false);
        }

        // 2) 鎖所有皮膚層為開（toggleModelPart 會一併 broadcastOptions 給伺服器）
        Options opt = mc.options;
        for (PlayerModelPart part : PlayerModelPart.values()) {
            if (!opt.isModelPartEnabled(part)) {
                opt.toggleModelPart(part, true);
            }
        }

        // 3) 靜步：偵測按鍵、取消衝刺、同步狀態
        LocalPlayer p = mc.player;
        boolean active = p != null
                && XCKeys.SILENT_WALK.isDown()
                && !p.isSpectator()
                && !p.getAbilities().flying
                && !p.isPassenger()
                && !p.onClimbable();
        SilentState.selfActive = active;

        if (active && p.isSprinting()) {
            p.setSprinting(false);
        }

        if (active != lastSent) {
            lastSent = active;
            ClientPacketListener conn = mc.getConnection();
            if (conn != null && XCNet.CHANNEL.isRemotePresent(conn.getConnection())) {
                XCNet.CHANNEL.sendToServer(new C2SSilentWalk(active));
            }
        }
    }

    @SubscribeEvent
    public static void onInput(MovementInputUpdateEvent e) {
        if (!SilentState.selfActive) return;
        e.getInput().forwardImpulse *= WALK_FACTOR;
        e.getInput().leftImpulse *= WALK_FACTOR;
    }

    // 原版腳步聲（走 Level.playSound）
    @SubscribeEvent
    public static void onLevelSound(PlayLevelSoundEvent.AtPosition e) {
        if (e.getSound() == null || !e.getLevel().isClientSide()) return;
        if (!e.getSound().value().getLocation().getPath().contains("step")) return;
        net.minecraft.world.phys.Vec3 pos = e.getPosition();
        if (nearSilentWalker(pos.x, pos.y, pos.z)) {
            e.setSound(null);
        }
    }

    // 模組腳步聲：PresenceFootsteps 直接走 SoundManager，不經 Level.playSound，
    // 改在 SoundEngine 層攔。命名空間 presencefootsteps(含 mono) 或原版 *step* 都算腳步。
    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent e) {
        SoundInstance s = e.getSound();
        if (s == null) return;
        net.minecraft.resources.ResourceLocation loc = s.getLocation();
        boolean footstep = loc.getNamespace().startsWith("presencefootsteps")
                || loc.getPath().contains("step");
        if (!footstep) return;
        if (nearSilentWalker(s.getX(), s.getY(), s.getZ())) {
            e.setSound(null);
        }
    }

    private static boolean nearSilentWalker(double x, double y, double z) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return false;
        UUID localId = mc.player.getUUID();
        for (Player pl : mc.level.players()) {
            if (pl.distanceToSqr(x, y, z) < 0.5D && SilentState.isSilent(pl, localId)) {
                return true;
            }
        }
        return false;
    }

    private ClientHandler() {}
}
