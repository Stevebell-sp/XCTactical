package com.xiaocheng.xctactical.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

// 伺服器 -> 客戶端：廣播某位玩家的靜步狀態
public class S2CSilentWalk {
    private final UUID id;
    private final boolean silent;

    public S2CSilentWalk(UUID id, boolean silent) {
        this.id = id;
        this.silent = silent;
    }

    public static void encode(S2CSilentWalk m, FriendlyByteBuf buf) {
        buf.writeUUID(m.id);
        buf.writeBoolean(m.silent);
    }

    public static S2CSilentWalk decode(FriendlyByteBuf buf) {
        return new S2CSilentWalk(buf.readUUID(), buf.readBoolean());
    }

    public static void handle(S2CSilentWalk m, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.xiaocheng.xctactical.client.SilentState.setRemote(m.id, m.silent)));
        c.setPacketHandled(true);
    }
}
