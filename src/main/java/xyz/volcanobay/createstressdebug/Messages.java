package xyz.volcanobay.createstressdebug;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import xyz.volcanobay.createstressdebug.packets.DebugDataS2C;
import xyz.volcanobay.createstressdebug.packets.StartDebuggingC2S;

public class Messages {

    private static SimpleChannel INSTANCE;

    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(Createstressdebug.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(StartDebuggingC2S.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(StartDebuggingC2S::new)
                .encoder(StartDebuggingC2S::toBytes)
                .consumerMainThread(StartDebuggingC2S::handle)
                .add();
        net.messageBuilder(DebugDataS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(DebugDataS2C::new)
                .encoder(DebugDataS2C::toBytes)
                .consumerMainThread(DebugDataS2C::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}
