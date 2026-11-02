package eu.eugenioguidetti.mcnetworking.webui.handlers;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 07/10/2026
 */

import com.sun.net.httpserver.HttpExchange;
import eu.eugenioguidetti.mcnetworking.webui.WebServer;
import eu.eugenioguidetti.mcnetworking.webui.utils.I18nHelper;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import static eu.eugenioguidetti.mcnetworking.webui.utils.WebUiUtils.sendTextResponse;


/**
 *
 * @author Eugenio Guidetti
 */
public class StaticResourceHandler
{
    // Il percorso di base nel classpath (es. "/webui")
    private static final String RESOURCE_BASE_PATH = "/webui";

    /**
     * Serve un file statico grezzo (immagini, css, js, html senza placeholder).
     */
    public static void serveFile(String path, @NonNull HttpExchange exchange) throws IOException
    {
        serveTemplate(path, null, exchange);
    }

    /**
     * Serve un file statico sostituendo eventuali placeholder definiti come {{CHIAVE}}.
     *
     * @param path         Percorso relativo al file (es. "/blocco/dettaglio.html")
     * @param placeholders Mappa chiave-valore per la sostituzione (es. "NOME_BLOCCO" -> "Host_1")
     * @param exchange     Contesto HTTP
     */
    public static void serveTemplate(String path, Map<String, String> placeholders, @NonNull HttpExchange exchange) throws IOException
    {
        if (path == null || path.isEmpty())
        {
            path = "/index.html";
        }
        else if (!path.contains("."))
        {
            if (!path.endsWith("/"))
            {
                path += "/";
            }
            path += "index.html";
        }

        // Protezione Path Traversal
        if (path.contains(".."))
        {
            sendTextResponse("Richiesta non valida.", 400, exchange);
            return;
        }

        byte[] rawBytes, responseBytes;
        String mimeType = getMimeType(path);

        String fullResourcePath = RESOURCE_BASE_PATH + path;
        try (InputStream is = WebServer.class.getResourceAsStream(fullResourcePath))
        {
            if (is == null)
            {
                sendTextResponse("404 Not Found: " + path, 404, exchange);
                return;
            }

            rawBytes = is.readAllBytes();
        }

        Locale clientLocale = I18nHelper.resolveLocale();
        Map<String, String> allPlaceholders = I18nHelper.getTranslationMap(clientLocale);

        if (placeholders != null)
        {
            allPlaceholders.putAll(placeholders);
        }

        // Sostituzione eventuali placeholders
        if (isTextMimeType(mimeType))
        {
            String htmlContent = new String(rawBytes, StandardCharsets.UTF_8);

            for (var entry : allPlaceholders.entrySet())
            {
                String targetPlaceholder = "{{" + entry.getKey() + "}}";
                String replacementValue = entry.getValue() != null ? entry.getValue() : "MISSING_REPLACEMENT_VALUE";
                htmlContent = htmlContent.replace(targetPlaceholder, replacementValue);
            }

            responseBytes = htmlContent.getBytes(StandardCharsets.UTF_8);
        }
        else
        {
            responseBytes = rawBytes;
        }

        exchange.getResponseHeaders().set("Content-Type", mimeType);
        exchange.sendResponseHeaders(200, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody())
        {
            os.write(responseBytes);
        }
    }

    private static boolean isTextMimeType(@NonNull String mimeType)
    {
        return mimeType.startsWith("text/") || mimeType.contains("javascript") || mimeType.contains("json");
    }

    private static @NonNull String getMimeType(@NonNull String path)
    {
        if (path.endsWith(".html") || path.endsWith(".htm"))
        {
            return "text/html; charset=UTF-8";
        }
        if (path.endsWith(".css"))
        {
            return "text/css; charset=UTF-8";
        }
        if (path.endsWith(".js"))
        {
            return "application/javascript; charset=UTF-8";
        }
        if (path.endsWith(".json"))
        {
            return "application/json; charset=UTF-8";
        }
        if (path.endsWith(".png"))
        {
            return "image/png";
        }
        if (path.endsWith(".jpg") || path.endsWith(".jpeg"))
        {
            return "image/jpeg";
        }
        if (path.endsWith(".svg"))
        {
            return "image/svg+xml";
        }
        if (path.endsWith(".ico"))
        {
            return "image/x-icon";
        }
        return "application/octet-stream";
    }
}