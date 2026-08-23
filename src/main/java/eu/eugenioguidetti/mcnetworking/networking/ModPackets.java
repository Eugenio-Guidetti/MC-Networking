package eu.eugenioguidetti.mcnetworking.networking;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 21/08/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.networking.packet.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class ModPackets
{
    private static void registerClientbound(@NonNull PayloadTypeRegistry<RegistryFriendlyByteBuf> registry)
    {
        // S -> C

        registry.register(OpenTerminalS2CPayload.TYPE, OpenTerminalS2CPayload.CODEC);
        registry.register(TerminalOutputS2CPayload.TYPE, TerminalOutputS2CPayload.CODEC);
        registry.register(CommandCompletionS2CPayload.TYPE, CommandCompletionS2CPayload.CODEC);

        // I GlobalReceiver vengono registrati solo sul client nella classe ClientBoundPackets
    }

    private static void registerServerbound(@NonNull PayloadTypeRegistry<RegistryFriendlyByteBuf> registry)
    {
        // C -> S

        registry.register(TerminalCommandC2SPayload.TYPE, TerminalCommandC2SPayload.CODEC);
        registry.register(TerminalSignalC2SPayload.TYPE, TerminalSignalC2SPayload.CODEC);
        registry.register(CommandCompletionC2SPayload.TYPE, CommandCompletionC2SPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TerminalCommandC2SPayload.TYPE, ServerboundPackets::handleTerminalCommandC2SPacket);
        ServerPlayNetworking.registerGlobalReceiver(TerminalSignalC2SPayload.TYPE, ServerboundPackets::handleTerminalSignalC2SPacket);
        ServerPlayNetworking.registerGlobalReceiver(CommandCompletionC2SPayload.TYPE,
                                                    ServerboundPackets::handleCommandCompletionC2SPayload);
    }

    public static void registerPackets()
    {
        MCNetworking.LOGGER.info("Registering Mod Packets: " + MCNetworking.MOD_ID);

        registerClientbound(PayloadTypeRegistry.clientboundPlay());
        registerServerbound(PayloadTypeRegistry.serverboundPlay());
    }
}
