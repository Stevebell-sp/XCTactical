package com.xiaocheng.xctactical.net;

import com.xiaocheng.xctactical.server.ServerHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// 客戶端 -> 伺服器：回報自己是否正在靜步
public class C2SSilentWalk {
    private final boolean active;

    public C2SSilentWalk(boolean active) {
        this.active = active;
    }

    public static void encode(C2SSilentWalk m, FriendlyByteBuf buf) {
        buf.writeBoolean(m.active);
    }

    public static C2SSilentWalk decode(FriendlyByteBuf buf) {
        return new C2SSilentWalk(buf.readBoolean());
    }

    public static void handle(C2SSilentWalk m, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer sender = c.getSender();
            if (sender != null) {
                ServerHandler.setSilent(sender, m.active);
            }
        });
        c.setPacketHandled(true);
    }
}
