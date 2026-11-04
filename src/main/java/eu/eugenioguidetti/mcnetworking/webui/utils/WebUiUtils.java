package eu.eugenioguidetti.mcnetworking.webui.utils;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 03/11/2026
 */

import com.sun.net.httpserver.HttpExchange;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 *
 * @author Eugenio Guidetti
 */
public class WebUiUtils
{
    public static @NonNull Map<String, String> queryToMap(String query)
    {
        Map<String, String> result = new HashMap<>();

        if (query == null)
        {
            return result;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs)
        {
            String[] keyValue = pair.split("=");
            if (keyValue.length > 1)
            {
                result.put(keyValue[0], keyValue[1]);
            }
            else
            {
                result.put(keyValue[0], ""); // Se non c'è un valore dopo "="
            }
        }

        return result;
    }


    public static void sendTextResponse(@NonNull String content, int statusCode, @NonNull HttpExchange exchange) throws IOException
    {
        byte[] payload = content.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, payload.length);
        try (OutputStream os = exchange.getResponseBody())
        {
            os.write(payload);
        }
    }

    public static Optional<GlobalPos> getPos(@NonNull HttpExchange exchange) throws IOException
    {
        if (Minecraft.getInstance().level == null)
        {
            sendTextResponse("Nessuna partita attiva nel client", 503, exchange);
            return Optional.empty();
        }

        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());

        if (!params.containsKey("x") || !params.containsKey("y") || !params.containsKey("z"))
        {
            sendTextResponse("Coordinate mancanti", 400, exchange);
            return Optional.empty();
        }

        try
        {
            int x = Integer.parseInt(params.get("x"));
            int y = Integer.parseInt(params.get("y"));
            int z = Integer.parseInt(params.get("z"));
            GlobalPos pos = new GlobalPos(Minecraft.getInstance().level.dimension(), new BlockPos(x, y, z));
            return Optional.of(pos);
        }
        catch (NumberFormatException e)
        {
            sendTextResponse("Parametri non validi: le coordinate devono essere numeri interi", 400, exchange);
            return Optional.empty();
        }
    }
}
