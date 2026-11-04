package eu.eugenioguidetti.mcnetworking.webui.handlers.cli;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 04/11/2026
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import eu.eugenioguidetti.mcnetworking.networking.packet.TerminalSignalC2SPayload;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalSignal;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.GlobalPos;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.Optional;

import static eu.eugenioguidetti.mcnetworking.webui.utils.WebUiUtils.*;

/**
 *
 * @author Eugenio Guidetti
 */
public class CliSignalHandler implements HttpHandler
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

        try
        {
            String signal = queryToMap(exchange.getRequestURI().getQuery()).get("signal");
            ClientPlayNetworking.send(new TerminalSignalC2SPayload(TerminalSignal.valueOf(signal), pos.orElse(null)));
        }
        catch (Exception e)
        {
            sendTextResponse("Segnale sconosciuto", 400, exchange);
        }

        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }
}
