package com.simpleskins.simpleskins;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * One server-to-client packet: who now wears which textures.
 * Empty value means "back to default".
 */
public final class SkinNetwork {
    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named(new ResourceLocation(SimpleSkins.MOD_ID, "main"))
            .networkProtocolVersion(() -> "1")
            .clientAcceptedVersions(version -> true)
            .serverAcceptedVersions(version -> true)
            .simpleChannel();

    private SkinNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(SkinSyncPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SkinSyncPacket::encode)
                .decoder(SkinSyncPacket::new)
                .consumerMainThread(SkinSyncPacket::handle)
                .add();
    }

    public record SkinSyncPacket(UUID playerId, String value, String signature) {
        public SkinSyncPacket(FriendlyByteBuf buf) {
            this(buf.readUUID(), buf.readUtf(32767), buf.readUtf(32767));
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeUUID(playerId);
            buf.writeUtf(value == null ? "" : value);
            buf.writeUtf(signature == null ? "" : signature);
        }

        public static void handle(SkinSyncPacket packet, Supplier<net.minecraftforge.network.NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(
                    net.minecraftforge.api.distmarker.Dist.CLIENT,
                    () -> () -> ClientSkinApplier.apply(packet.playerId(), packet.value(), packet.signature())));
            ctx.get().setPacketHandled(true);
        }
    }
}
