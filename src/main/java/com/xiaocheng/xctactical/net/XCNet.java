package com.xiaocheng.xctactical.net;

import com.xiaocheng.xctactical.XCTactical;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class XCNet {
    private static final String PROTO = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(XCTactical.MODID, "main"),
            () -> PROTO, PROTO::equals, PROTO::equals);

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, C2SSilentWalk.class,
                C2SSilentWalk::encode, C2SSilentWalk::decode, C2SSilentWalk::handle);
        CHANNEL.registerMessage(id++, S2CSilentWalk.class,
                S2CSilentWalk::encode, S2CSilentWalk::decode, S2CSilentWalk::handle);
    }

    private XCNet() {}
}
