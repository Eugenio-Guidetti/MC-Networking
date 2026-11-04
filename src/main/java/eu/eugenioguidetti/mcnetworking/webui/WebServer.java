package eu.eugenioguidetti.mcnetworking.webui;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 05/10/2026
 */

import com.sun.net.httpserver.HttpServer;
import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.webui.handlers.DeviceListHandler;
import eu.eugenioguidetti.mcnetworking.webui.handlers.StaticResourceHandler;
import eu.eugenioguidetti.mcnetworking.webui.handlers.cli.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import static eu.eugenioguidetti.mcnetworking.GlobalConstants.WEBUI_PORT;


/**
 *
 * @author Eugenio Guidetti
 */
public class WebServer
{
    private static HttpServer server = null;

    public static void startServer() throws IOException
    {
        if (server != null)
        {
            MCNetworking.LOGGER.warn("Web Server già avviato");

            return;
        }

        server = HttpServer.create(new InetSocketAddress("localhost", WEBUI_PORT), 0);

        server.createContext("/", (exchange ->
        {
            String path = exchange.getRequestURI().getPath();
            StaticResourceHandler.serveFile(path, exchange);
        }));

        server.createContext("/device", new DeviceListHandler());
        server.createContext("/device/cli", new CliHandler());
        server.createContext("/device/cli/stream", new CliStreamHandler());
        server.createContext("/device/cli/send", new CliSendHandler());
        server.createContext("/device/cli/signal", new CliSignalHandler());
        server.createContext("/device/cli/complete", new CliCompleteHandler());

        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        server.start();
    }

    public static void stopServer()
    {
        if (server != null)
        {
            // Il parametro '0' indica di chiudere immediatamente senza attendere altre richieste
            server.stop(0);

            server = null;

            MCNetworking.LOGGER.info("Web Server fermato con successo");
        }
    }
}
