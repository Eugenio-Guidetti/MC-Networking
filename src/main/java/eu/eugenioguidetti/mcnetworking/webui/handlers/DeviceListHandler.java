package eu.eugenioguidetti.mcnetworking.webui.handlers;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 06/10/2026
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import eu.eugenioguidetti.mcnetworking.webui.ClientWorldScanner;
import eu.eugenioguidetti.mcnetworking.webui.utils.I18nHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static eu.eugenioguidetti.mcnetworking.webui.utils.WebUiUtils.sendTextResponse;

/**
 *
 * @author Eugenio Guidetti
 */

public class DeviceListHandler implements HttpHandler
{
    @Override
    public void handle(@NonNull HttpExchange exchange) throws IOException
    {
        if (Minecraft.getInstance().level == null)
        {
            sendTextResponse("Nessuna partita attiva nel client", 503, exchange);
            return;
        }

        String response;
        int statusCode;

        try
        {
            serveBlocksList(exchange);

            return;
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


    private void serveBlocksList(@NonNull HttpExchange exchange) throws Exception
    {
        CompletableFuture<Map<BlockPos, ClientWorldScanner.DeviceSummary>> loadedDevicesFuture = ClientWorldScanner.getLoadedDevicesFuture();
        Map<BlockPos, ClientWorldScanner.DeviceSummary> loadedDevices = loadedDevicesFuture.get(3, TimeUnit.SECONDS);


        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("TABELLA", getLoadedDevicesHtml(loadedDevices));

        StaticResourceHandler.serveTemplate("/device/index.html", placeholders, exchange);
    }


    private static @NonNull String getLoadedDevicesHtml(@NonNull Map<BlockPos, ClientWorldScanner.DeviceSummary> loadedDevices)
    {
        Locale clientLocale = I18nHelper.resolveLocale();
        Map<String, String> translationMap = I18nHelper.getTranslationMap(clientLocale);

        if (loadedDevices.isEmpty())
        {
            return translationMap.get("MSG_DEVICE_INDEX_EMPTY");
        }

        StringBuilder sb = new StringBuilder();

        sb.append("<div class=\"tablecontainer\"><table><tr><th>");
        sb.append(translationMap.get("MSG_DEVICE_INDEX_DEVICE_HOSTNAME"));
        sb.append("</th><th>");
        sb.append(translationMap.get("MSG_DEVICE_INDEX_DEVICE_TYPE"));
        sb.append("</th><th>");
        sb.append(translationMap.get("MSG_DEVICE_INDEX_DEVICE_POS"));
        sb.append("</th><th>");
        sb.append(translationMap.get("MSG_DEVICE_INDEX_AZIONI"));
        sb.append("</th></tr>");

        List<ClientWorldScanner.DeviceSummary> loadedDevicesValues = loadedDevices
                .values()
                .stream()
                .sorted(Comparator.comparing(ClientWorldScanner.DeviceSummary::hostname))
                .toList();


        for (var entry : loadedDevicesValues)
        {
            sb.append("<tr>");

            sb.append("<td>");
            sb.append(entry.hostname());
            sb.append("</td>");

            sb.append("<td>");
            sb.append(entry.tipo());
            sb.append("</td>");

            sb.append("<td>");
            sb.append(entry.pos().toShortString());
            sb.append("</td>");

            sb.append("<td><a href = \"/device/cli?x=");
            sb.append(entry.pos().getX());
            sb.append("&y=");
            sb.append(entry.pos().getY());
            sb.append("&z=");
            sb.append(entry.pos().getZ());
            sb.append("\">");
            sb.append(translationMap.get("MSG_DEVICE_INDEX_OPEN_CLI"));
            sb.append("</a></td>");

            sb.append("</tr>");
        }

        sb.append("</table></div>");

        return sb.toString();
    }
}
