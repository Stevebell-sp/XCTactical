package com.xiaocheng.xctactical.server;

import com.xiaocheng.xctactical.XCTactical;
import com.xiaocheng.xctactical.net.S2CSilentWalk;
import com.xiaocheng.xctactical.net.XCNet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = XCTactical.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerHandler {

    private static final Set<UUID> SILENT = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void setSilent(ServerPlayer from, boolean silent) {
        UUID id = from.getUUID();
        if (silent) SILENT.add(id); else SILENT.remove(id);
        broadcastExcept(from.getServer(), id, silent, id);
    }

    private static void broadcastExcept(MinecraftServer server, UUID id, boolean silent, UUID skip) {
        if (server == null) return;
        S2CSilentWalk pkt = new S2CSilentWalk(id, silent);
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            if (sp.getUUID().equals(skip)) continue;
            XCNet.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), pkt);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        for (UUID id : SILENT) {
            XCNet.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new S2CSilentWalk(id, true));
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        UUID id = sp.getUUID();
        if (SILENT.remove(id)) {
            broadcastExcept(sp.getServer(), id, false, id);
        }
    }

    private ServerHandler() {}
}
