package eu.eugenioguidetti.mcnetworking.networking;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 21/08/2026
 */

import eu.eugenioguidetti.mcnetworking.networking.packet.OpenTerminalS2CPayload;
import eu.eugenioguidetti.mcnetworking.networking.packet.TerminalOutputS2CPayload;
import eu.eugenioguidetti.mcnetworking.terminal.gui.TerminalScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class ClientboundPackets
{
    public static void registerReceivers()
    {
        ClientPlayNetworking.registerGlobalReceiver(OpenTerminalS2CPayload.TYPE, ClientboundPackets::handleOpenTerminalS2CPacket);
        ClientPlayNetworking.registerGlobalReceiver(TerminalOutputS2CPayload.TYPE, ClientboundPackets::handleTerminalOutputS2CPacket);
    }

    public static void handleOpenTerminalS2CPacket(@NonNull OpenTerminalS2CPayload payload, ClientPlayNetworking.@NonNull Context context)
    {
        Minecraft.getInstance().gui.setScreen(new TerminalScreen(payload));
    }

    public static void handleTerminalOutputS2CPacket(TerminalOutputS2CPayload payload, ClientPlayNetworking.@NonNull Context context)
    {
        Minecraft client = Minecraft.getInstance();

        // Controlliamo se il giocatore ha aperto la schermata giusta
        if (client.gui.screen() instanceof TerminalScreen terminalScreen)
        {
            // Aggiungiamo l'output ricevuto dal server allo storico della UI
            terminalScreen.updateOutput(payload);
        }
    }
}
