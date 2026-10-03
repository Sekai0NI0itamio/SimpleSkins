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

    private static void applyTo(PlayerInfo info, String value, String signature) {
        GameProfile profile = info.getProfile();
        profile.getProperties().removeAll("textures");
        if (value != null && !value.isEmpty()) {
            String signatureOrNull = signature == null || signature.isEmpty() ? null : signature;
            profile.getProperties().put("textures", new Property("textures", value, signatureOrNull));
        }
        info.registerTextures();
    }
}
