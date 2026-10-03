package com.simpleskins.simpleskins;

import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(SimpleSkins.MOD_ID)
public class SimpleSkins {
    public static final String MOD_ID = "simpleskins";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SimpleSkins() {
        SkinNetwork.register();
        MinecraftForge.EVENT_BUS.register(SkinEvents.class);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(ClientEvents.class);
        }
    }
}
