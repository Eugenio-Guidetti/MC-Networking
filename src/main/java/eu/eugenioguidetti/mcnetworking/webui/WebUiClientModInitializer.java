package eu.eugenioguidetti.mcnetworking.webui;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 05/10/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

/**
 *
 * @author Eugenio Guidetti
 */
public class WebUiClientModInitializer implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        new Thread(() ->
                   {
                       try
                       {
                           WebServer.startServer();
                           System.out.println("Web Server avviato con successo!");
                       }
                       catch (Exception e)
                       {
                           MCNetworking.LOGGER.error("Errore durante l'avvio del server", e);
                       }
                   }).start();

        ClientLifecycleEvents.CLIENT_STOPPING.register(client ->
                                                       {
                                                           WebServer.stopServer();
                                                       });
    }
}
