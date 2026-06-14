package com.xiaocheng.xctactical;

import com.xiaocheng.xctactical.net.XCNet;
import net.minecraftforge.fml.common.Mod;

@Mod(XCTactical.MODID)
public class XCTactical {
    public static final String MODID = "xctactical";

    public XCTactical() {
        XCNet.register();
    }
}
