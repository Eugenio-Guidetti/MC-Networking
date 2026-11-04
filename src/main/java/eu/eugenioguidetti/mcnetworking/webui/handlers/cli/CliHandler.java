package eu.eugenioguidetti.mcnetworking.webui.handlers.cli;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 02/11/2026
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.networking.packet.WebuiCliHistoryS2CPayload;
import eu.eugenioguidetti.mcnetworking.webui.ClientWorldScanner;
import eu.eugenioguidetti.mcnetworking.webui.handlers.StaticResourceHandler;
import eu.eugenioguidetti.mcnetworking.webui.utils.WebUiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static eu.eugenioguidetti.mcnetworking.webui.utils.WebUiUtils.queryToMap;
import static eu.eugenioguidetti.mcnetworking.webui.utils.WebUiUtils.sendTextResponse;

/**
 *
 * @author Eugenio Guidetti
 */
public class CliHandler implements HttpHandler
{
    @Override
    public void handle(HttpExchange exchange) throws IOException
    {
        if (Minecraft.getInstance().level == null)
        {
            sendTextResponse("Nessuna partita attiva nel client", 503, exchange);
            return;
        }

        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        String response;
        int statusCode;

        Optional<GlobalPos> optionalPos = WebUiUtils.getPos(exchange);
        if (optionalPos.isEmpty())
        {
            return;
        }
        GlobalPos pos = optionalPos.get();

        try
        {
            serveCli(pos.pos(), exchange);
            return;
        }
        catch (NumberFormatException e)
        {
            statusCode = 400;
            response = "Parametri non validi: le coordinate devono essere numeri interi";
        }
        catch (TimeoutException e)
        {
            statusCode = 504;
            response = "Gateway Timeout: il thread di Minecraft non ha risposto in tempo";
        }
        catch (Exception e)
        {
            statusCode = 500;
            response = "Errore interno: " + e.getMessage();
        }

        sendTextResponse(response, statusCode, exchange);
    }


    private void serveCli(@NonNull BlockPos pos, @NonNull HttpExchange exchange) throws Exception
    {
        CompletableFuture<BlockEntity> blockEntityFuture = ClientWorldScanner.getBlockEntityFuture(pos);

        // Attesa con timeout per non bloccare il worker HTTP all'infinito
        BlockEntity entity = blockEntityFuture.get(3, TimeUnit.SECONDS);

        if (!(entity instanceof NetworkingBlockEntity netEntity))
        {
            sendTextResponse("Nessun dispositivo MC-Networking trovato in " + pos.toShortString(), 404, exchange);
            return;
        }


        CompletableFuture<WebuiCliHistoryS2CPayload> blockEntityCliHistoryFuture = ClientWorldScanner.getBlockEntityCliHistoryFuture(pos);
        WebuiCliHistoryS2CPayload payload = blockEntityCliHistoryFuture.get(3, TimeUnit.SECONDS);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("CLI_HISTORY", getHistoryHtml(payload.history()));
        placeholders.put("CLI_ROW_STYLE_DISPLAY", payload.showPrompt() ? "" : "style=\"display: none;\"");
        placeholders.put("CLI_PROMPT", payload.currentPrompt());
        StaticResourceHandler.serveTemplate("/device/cli/index.html", placeholders, exchange);
    }

    private static @NonNull String getHistoryHtml(@NonNull List<String> history)
    {
        if (history.isEmpty())
        {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size() - 1; i++)
        {
            String line = history.get(i);
            sb.append(line);
            sb.append("\n");
        }
        sb.append(history.getLast());

        return sb.toString();
    }


    private void redirectToDeviceList(HttpExchange exchange) throws IOException
    {
        StaticResourceHandler.serveFile("/device/redirect.html", exchange);
    }
}
