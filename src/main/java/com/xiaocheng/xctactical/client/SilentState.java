package com.xiaocheng.xctactical.client;

import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 客戶端：誰正在靜步。自己看本地按鍵狀態，其他玩家看伺服器同步來的集合。
public final class SilentState {

    public static volatile boolean selfActive = false;

    private static final Set<UUID> REMOTE = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void setRemote(UUID id, boolean silent) {
        if (silent) REMOTE.add(id); else REMOTE.remove(id);
    }

    public static boolean isSilent(Player p, UUID localId) {
        if (p == null) return false;
        if (p.getUUID().equals(localId)) return selfActive;
        return REMOTE.contains(p.getUUID());
    }

    private SilentState() {}
}
