package eu.eugenioguidetti.mcnetworking.webui.handlers;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 06/10/2026
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.minecraft.client.Minecraft;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static eu.eugenioguidetti.mcnetworking.webui.utils.WebUiUtils.sendTextResponse;

/**
 *
 * @author Eugenio Guidetti
 */
public class GameModeHandler implements HttpHandler
{
    @Override
    public void handle(@NonNull HttpExchange exchange) throws IOException
    {
        String response;
        int statusCode = 200;

        try
        {
            response = getGameMode();
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

    private String getGameMode() throws Exception
    {
        CompletableFuture<String> gameModeQueryFuture = new CompletableFuture<>();

        Minecraft client = Minecraft.getInstance();
        client.execute(() ->
                       {
                           try
                           {
                               if (client.gameMode == null)
                               {
                                   gameModeQueryFuture.complete("UNKNOWN (Non in partita)");
                                   return;
                               }

                               gameModeQueryFuture.complete(client.gameMode.getPlayerMode().toString());
                           }
                           catch (Exception e)
                           {
                               gameModeQueryFuture.completeExceptionally(e);
                           }
                       });

        // Attesa con timeout per non bloccare il worker HTTP all'infinito
        return gameModeQueryFuture.get(3, TimeUnit.SECONDS);
    }
}

