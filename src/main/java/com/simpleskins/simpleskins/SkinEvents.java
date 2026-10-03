package com.simpleskins.simpleskins;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public final class SkinEvents {
    private static SkinStore store;
    private static Path file;

    private SkinEvents() {
    }

    static void apply(UUID id, String skinName, String value, String signature) {
        if (store == null) {
            return;
        }
        store.put(id, skinName, value, signature);
        save();
        SkinNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(),
                new SkinNetwork.SkinSyncPacket(id, value, signature));
    }

    static void clear(UUID id) {
        if (store == null) {
            return;
        }
        store.remove(id);
        save();
        SkinNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(),
                new SkinNetwork.SkinSyncPacket(id, "", ""));
    }

    private static void save() {
        if (store == null) {
            return;
        }
        try {
            store.save();
        } catch (IOException e) {
            SimpleSkins.LOGGER.error("Failed to save SimpleSkins data", e);
        }
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        file = server.getWorldPath(LevelResource.ROOT).resolve("serverconfig").resolve("simpleskins.json");
        store = new SkinStore(file);
        try {
            store.load();
        } catch (IOException e) {
            SimpleSkins.LOGGER.error("Failed to load SimpleSkins data from {}", file, e);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        save();
        store = null;
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        SkinCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (store == null || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        for (Map.Entry<UUID, SkinStore.Entry> entry : store.all().entrySet()) {
            SkinNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new SkinNetwork.SkinSyncPacket(entry.getKey(),
                            entry.getValue().value, entry.getValue().signature));
        }
        SkinStore.Entry own = store.get(player.getUUID());
        if (own != null) {
            SkinNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(),
                    new SkinNetwork.SkinSyncPacket(player.getUUID(), own.value, own.signature));
        }
    }
}
