package eu.eugenioguidetti.mcnetworking.webui.handlers.cli;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 03/11/2026
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import eu.eugenioguidetti.mcnetworking.networking.packet.TerminalCommandC2SPayload;
import eu.eugenioguidetti.mcnetworking.webui.utils.WebUiUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.GlobalPos;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static eu.eugenioguidetti.mcnetworking.webui.utils.WebUiUtils.sendTextResponse;

/**
 *
 * @author Eugenio Guidetti
 */
public class CliSendHandler implements HttpHandler
{
    @Override
    public void handle(@NonNull HttpExchange exchange) throws IOException
    {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST"))
        {
            exchange.getResponseHeaders().set("Allow", "POST");
            sendTextResponse("Metodo errato", 405, exchange);
            return;
        }

        Optional<GlobalPos> pos = WebUiUtils.getPos(exchange);
        if (pos.isEmpty())
        {
            return;
        }

        byte[] bodyBytes = exchange.getRequestBody().readAllBytes();
        String comando = new String(bodyBytes, StandardCharsets.UTF_8).trim();

        ClientPlayNetworking.send(new TerminalCommandC2SPayload(comando, pos.orElse(null)));

        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }
}
