package com.simpleskins.simpleskins;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SkinCommands {
    private SkinCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("skin")
                .requires(source -> true)
                .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> setSelf(ctx.getSource(),
                                StringArgumentType.getString(ctx, "name"))))
                .then(Commands.literal("clear")
                        .executes(ctx -> clearSelf(ctx.getSource())))
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .executes(ctx -> setOther(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "player"),
                                                StringArgumentType.getString(ctx, "name")))))));
    }

    private static ServerPlayer playerOf(CommandSourceStack source) {
        return source.getEntity() instanceof ServerPlayer player ? player : null;
    }

    private static int setSelf(CommandSourceStack source, String name) {
        ServerPlayer player = playerOf(source);
        if (player == null) {
            source.sendFailure(Component.literal("Only players can wear skins."));
            return 0;
        }
        if (!MojangFetch.validName(name)) {
            source.sendFailure(Component.literal("'" + name + "' is not a valid username."));
            return 0;
        }
        fetchAndApply(source, player.getUUID(), name);
        return 1;
    }

    private static int clearSelf(CommandSourceStack source) {
        ServerPlayer player = playerOf(source);
        if (player == null) {
            source.sendFailure(Component.literal("Only players can clear skins."));
            return 0;
        }
        SkinEvents.clear(player.getUUID());
        source.sendSuccess(() -> Component.literal("Skin cleared.").withStyle(ChatFormatting.YELLOW), false);
        return 1;
    }

    private static int setOther(CommandSourceStack source, String playerName, String skinName) {
        if (!MojangFetch.validName(skinName)) {
            source.sendFailure(Component.literal("'" + skinName + "' is not a valid username."));
            return 0;
        }
        ServerPlayer target = source.getServer().getPlayerList().getPlayerByName(playerName);
        UUID id = target != null ? target.getUUID() : offlineId(playerName);
        fetchAndApply(source, id, skinName);
        return 1;
    }

    static UUID offlineId(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    private static void fetchAndApply(CommandSourceStack source, UUID id, String name) {
        source.sendSuccess(() -> Component.literal("Fetching skin for '" + name + "'...").withStyle(ChatFormatting.GRAY), false);
        CompletableFuture.supplyAsync(() -> MojangFetch.fetch(name)).thenAccept(skin -> {
            if (source.getServer() == null) {
                return;
            }
            source.getServer().execute(() -> {
                if (skin == null) {
                    source.sendFailure(Component.literal("No premium skin found for '" + name + "'."));
                    return;
                }
                SkinEvents.apply(id, name, skin.value(), skin.signature());
                source.sendSuccess(() -> Component.literal("Now wearing " + name + "'s skin.").withStyle(ChatFormatting.GREEN), false);
            });
        });
    }
}
