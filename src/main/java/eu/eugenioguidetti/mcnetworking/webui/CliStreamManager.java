package eu.eugenioguidetti.mcnetworking.webui;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 03/11/2026
 */

import com.google.gson.Gson;
import eu.eugenioguidetti.mcnetworking.networking.packet.TerminalOutputS2CPayload;
import eu.eugenioguidetti.mcnetworking.webui.dto.CliUpdate;
import net.minecraft.core.GlobalPos;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Eugenio Guidetti
 */
public class CliStreamManager
{
    // Mappa: Posizione del blocco -> Lista di code attive dei client web
    private static final Map<GlobalPos, List<BlockingQueue<String>>> SUBSCRIBERS = new ConcurrentHashMap<>();
    private static final Gson GSON = new Gson();

    /**
     * Iscrive una connessione HTTP agli aggiornamenti di un blocco specifico.
     */
    public static @NonNull BlockingQueue<String> subscribe(@NonNull GlobalPos pos)
    {
        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        SUBSCRIBERS.computeIfAbsent(pos, k -> new CopyOnWriteArrayList<>()).add(queue);
        return queue;
    }

    /**
     * Rimuove l'iscrizione quando il browser chiude la scheda o la connessione cade.
     */
    public static void unsubscribe(GlobalPos pos, BlockingQueue<String> queue)
    {
        List<BlockingQueue<String>> list = SUBSCRIBERS.get(pos);
        if (list != null)
        {
            list.remove(queue);
            if (list.isEmpty())
            {
                SUBSCRIBERS.remove(pos);
            }
        }
    }

    /**
     * Invia a tutti i browser connessi il nuovo contenuto della CLI.
     */
    public static void broadcast(@NonNull TerminalOutputS2CPayload payload)
    {
        GlobalPos pos = payload.globalPos();

        StringBuilder outputBuilder = new StringBuilder();

        List<String> screenBuffer = payload.screenBuffer();
        for (int i = 0; i < screenBuffer.size() - 1; i++)
        {
            String line = screenBuffer.get(i);
            outputBuilder.append(line);
            outputBuilder.append("\n");
        }
        if (!screenBuffer.isEmpty())
        {
            outputBuilder.append(screenBuffer.getLast());
        }

        CliUpdate update = new CliUpdate(outputBuilder.toString(), payload.showPrompt(), payload.prompt());
        String jsonPayload = GSON.toJson(update);


        List<BlockingQueue<String>> list = SUBSCRIBERS.get(pos);
        if (list != null)
        {
            for (BlockingQueue<String> queue : list)
            {
                queue.offer(jsonPayload);
            }
        }
    }
}
