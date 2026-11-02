package eu.eugenioguidetti.mcnetworking.webui.handlers;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 02/11/2026
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.webui.ClientWorldScanner;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        try
        {
            if (!params.containsKey("x") || !params.containsKey("y") || !params.containsKey("z"))
            {
                redirectToDeviceList(exchange);
            }
            else
            {
                int x = Integer.parseInt(params.get("x"));
                int y = Integer.parseInt(params.get("y"));
                int z = Integer.parseInt(params.get("z"));
                BlockPos pos = new BlockPos(x, y, z);

                serveCli(pos, exchange);
            }

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


        CompletableFuture<List<String>> blockEntityCliHistoryFuture = ClientWorldScanner.getBlockEntityCliHistoryFuture(pos);
        List<String> history = blockEntityCliHistoryFuture.get(3, TimeUnit.SECONDS);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("CLI_HISTORY", getHistoryHtml(history));
        StaticResourceHandler.serveTemplate("/device/cli/index.html", placeholders, exchange);
    }

    private static @NonNull String getHistoryHtml(@NonNull List<String> history)
    {
        if (history.isEmpty())
        {
            return "Nessun comando inviato";
        }

        StringBuilder sb = new StringBuilder();
        for (String command : history)
        {
            sb.append(command);
            sb.append("<br>");
        }

        return sb.toString();
    }


    private void redirectToDeviceList(HttpExchange exchange) throws IOException
    {
        StaticResourceHandler.serveFile("/device/redirect.html", exchange);
    }
}
