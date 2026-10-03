package com.simpleskins.simpleskins.mixin;

import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Calls vanilla's own texture registration after a skin swap. Names are
 * Mojang names here; the Mixin annotation processor remaps them for release.
 */
@Mixin(PlayerInfo.class)
public interface PlayerInfoAccessor {
    @Invoker("registerTextures")
    void simpleskins$refreshTextures();
}
