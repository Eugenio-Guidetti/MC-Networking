package eu.eugenioguidetti.mcnetworking.webui.handlers.cli;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 03/11/2026
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import eu.eugenioguidetti.mcnetworking.webui.CliStreamManager;
import eu.eugenioguidetti.mcnetworking.webui.utils.WebUiUtils;
import net.minecraft.core.GlobalPos;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author Eugenio Guidetti
 */
public class CliStreamHandler implements HttpHandler
{
    @Override
    public void handle(@NonNull HttpExchange exchange) throws IOException
    {
        Optional<GlobalPos> optionalPos = WebUiUtils.getPos(exchange);
        if (optionalPos.isEmpty())
        {
            return;
        }
        GlobalPos pos = optionalPos.get();

        // Header obbligatori per Server-Sent Events
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream; charset=UTF-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.getResponseHeaders().set("Connection", "keep-alive");

        // Il parametro '0' abilita il chunked transfer encoding (stream a dimensione indefinita)
        exchange.sendResponseHeaders(200, 0);

        OutputStream os = exchange.getResponseBody();
        BlockingQueue<String> queue = CliStreamManager.subscribe(pos);

        try
        {
            while (!Thread.currentThread().isInterrupted())
            {
                String sseMessage = queue.take();
                os.write(sseMessage.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
        }
        catch (InterruptedException | IOException e)
        {
            // Il browser ha chiuso la pagina o la scheda è stata terminata
        }
        finally
        {
            CliStreamManager.unsubscribe(pos, queue);

            try
            {
                os.close();
            }
            catch (Exception ignored)
            {
            }
        }
    }
}
