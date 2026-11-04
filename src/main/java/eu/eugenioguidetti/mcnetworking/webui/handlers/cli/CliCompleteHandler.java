package eu.eugenioguidetti.mcnetworking.webui.handlers.cli;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 04/11/2026
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import eu.eugenioguidetti.mcnetworking.networking.packet.CommandCompletionC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.GlobalPos;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static eu.eugenioguidetti.mcnetworking.webui.utils.WebUiUtils.getPos;
import static eu.eugenioguidetti.mcnetworking.webui.utils.WebUiUtils.sendTextResponse;

/**
 *
 * @author Eugenio Guidetti
 */
public class CliCompleteHandler implements HttpHandler
{
    @Override
    public void handle(@NonNull HttpExchange exchange) throws IOException
    {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod()))
        {
            exchange.getResponseHeaders().set("Allow", "POST");
            sendTextResponse("Metodo errato", 405, exchange);
            return;
        }

        Optional<GlobalPos> pos = getPos(exchange);
        if (pos.isEmpty())
        {
            return;
        }

        byte[] bodyBytes = exchange.getRequestBody().readAllBytes();
        String draftCommand = new String(bodyBytes, StandardCharsets.UTF_8);

        ClientPlayNetworking.send(new CommandCompletionC2SPayload(draftCommand, pos.orElse(null)));

        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }
}
