package eu.eugenioguidetti.mcnetworking.terminal.initializers;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 07/06/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.TerminalCache;
import eu.eugenioguidetti.mcnetworking.terminal.gui.CommandHistoryCache;
import eu.eugenioguidetti.mcnetworking.terminal.gui.TerminalScreen;
import eu.eugenioguidetti.mcnetworking.terminal.packet.OpenTerminalS2CPacket;
import eu.eugenioguidetti.mcnetworking.terminal.packet.TerminalOutputS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

/**
 *
 * @author Eugenio Guidetti
 */
public class ClientTerminalPacketsInitializer implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        // Registriamo cosa fa il CLIENT quando riceve il pacchetto dal server
        ClientPlayNetworking.registerGlobalReceiver(TerminalOutputS2CPacket.ID, (payload, context) ->
        {

            // Come per il server, spostiamo l'esecuzione sul thread principale del Client
            context.client().execute(() ->
                                     {
                                         Minecraft client = Minecraft.getInstance();

                                         // Controlliamo se il giocatore ha aperto la schermata giusta
                                         if (client.screen instanceof TerminalScreen terminalScreen)
                                         {
                                             // Aggiungiamo l'output ricevuto dal server allo storico della UI
                                             terminalScreen.addOutput(payload.output(), payload.prompt());
                                         }
                                     });
        });

        // Apri la UI del terminale quando lo dice il server
        ClientPlayNetworking.registerGlobalReceiver(OpenTerminalS2CPacket.ID, (payload, context) ->
        {
            context.client().execute(() ->
                                     {
                                         Minecraft
                                                 .getInstance()
                                                 .setScreen(new TerminalScreen(context.player().level(),
                                                                               payload.pos(),
                                                                               payload.history(),
                                                                               payload.currentPrompt()));
                                     });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                                                       {
                                                           TerminalCache.clearAll();
                                                           CommandHistoryCache.clearAll();
                                                       });
    }
}
