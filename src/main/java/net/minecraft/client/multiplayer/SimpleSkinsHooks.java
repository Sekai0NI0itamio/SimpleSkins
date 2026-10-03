package net.minecraft.client.multiplayer;

/**
 * Lives in vanilla's package on purpose: PlayerInfo#registerTextures is
 * protected, and same-package access needs no access transformer, mixin, or
 * reflection. Called only with instances vanilla itself created.
 */
public final class SimpleSkinsHooks {
    private SimpleSkinsHooks() {
    }

    public static void refreshTextures(PlayerInfo info) {
        info.registerTextures();
    }
}
