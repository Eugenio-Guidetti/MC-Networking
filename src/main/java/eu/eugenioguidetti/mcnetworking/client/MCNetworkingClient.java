package eu.eugenioguidetti.mcnetworking.client;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 21/08/2026
 */

import eu.eugenioguidetti.mcnetworking.client.rendering.CablesRenderPipeline;
import eu.eugenioguidetti.mcnetworking.networking.ClientboundPackets;
import net.fabricmc.api.ClientModInitializer;

/**
 *
 * @author Eugenio Guidetti
 */
public class MCNetworkingClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        CablesRenderPipeline cablesRenderPipeline = new CablesRenderPipeline();
        ClientboundPackets.registerReceivers();
    }
}
