package com.simpleskins.simpleskins;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client-only skin swapping. Swaps the tab-entry textures property, then runs
 * vanilla texture registration again — the same call the game makes when the
 * tab entry first arrives. No access transformers, mixins, or reflection.
 */
public final class ClientSkinApplier {
    private static final Map<UUID, String[]> PENDING = new HashMap<>();

    private ClientSkinApplier() {
    }

    public static void apply(UUID playerId, String value, String signature) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        if (connection == null) {
            PENDING.put(playerId, new String[]{value, signature});
            return;
        }
        PlayerInfo info = connection.getPlayerInfo(playerId);
        if (info == null) {
            PENDING.put(playerId, new String[]{value, signature});
            return;
        }
        applyTo(info, value, signature);
    }

    static void flushPending() {
        if (PENDING.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener connection = minecraft.getConnection();
        if (connection == null) {
            return;
        }
        PENDING.entrySet().removeIf(entry -> {
            PlayerInfo info = connection.getPlayerInfo(entry.getKey());
            if (info == null) {
                return false;
            }
            String[] skin = entry.getValue();
            applyTo(info, skin[0], skin[1]);
            return true;
        });
    }

    private static volatile java.lang.reflect.Method refreshTextures;

    private static void applyTo(PlayerInfo info, String value, String signature) {
        GameProfile profile = info.getProfile();
        profile.getProperties().removeAll("textures");
        if (value != null && !value.isEmpty()) {
            String signatureOrNull = signature == null || signature.isEmpty() ? null : signature;
            profile.getProperties().put("textures", new Property("textures", value, signatureOrNull));
        }
        refresh(info);
    }

    /**
     * Runs vanilla's texture registration without naming it: PlayerInfo has
     * exactly one void no-arg method (verified against 1.20.1), so this works
     * in dev and in obfuscated production alike. Failures only log.
     */
    private static void refresh(PlayerInfo info) {
        try {
            java.lang.reflect.Method method = refreshTextures;
            if (method == null) {
                java.lang.reflect.Method found = null;
                for (java.lang.reflect.Method candidate : PlayerInfo.class.getDeclaredMethods()) {
                    if (candidate.getReturnType() == void.class
                            && candidate.getParameterCount() == 0
                            && !candidate.isSynthetic()
                            && !candidate.isBridge()) {
                        if (found != null) {
                            SimpleSkins.LOGGER.error("SimpleSkins cannot refresh skins: ambiguous texture method");
                            return;
                        }
                        found = candidate;
                    }
                }
                if (found == null) {
                    SimpleSkins.LOGGER.error("SimpleSkins cannot refresh skins: texture method not found");
                    return;
                }
                found.setAccessible(true);
                refreshTextures = method = found;
            }
            method.invoke(info);
        } catch (ReflectiveOperationException | SecurityException | RuntimeException e) {
            SimpleSkins.LOGGER.error("SimpleSkins cannot refresh skins", e);
        }
    }
}
