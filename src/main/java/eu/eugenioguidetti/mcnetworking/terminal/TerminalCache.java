package eu.eugenioguidetti.mcnetworking.terminal;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 07/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe solo server
 *
 * @author Eugenio Guidetti
 */
public class TerminalCache
{
    private static final int MAX_LINES = 100;
    // Mappa che associa le coordinate del blocco (e dimensione) e al giocatore al suo storico e sessione
    private static final Map<GlobalPos, CacheValue> cache = new HashMap<>();

    public static CacheValue getOrCreateSession(Level level, BlockPos pos)
    {
        if (level.isClientSide())
        {
            throw new IllegalStateException("Il Client sta cercando di accedere alla cache del Server");
        }

        // Creiamo la chiave univoca unendo la dimensione attuale e le coordinate
        GlobalPos key = GlobalPos.of(level.dimension(), pos);

        return cache.computeIfAbsent(key, k ->
        {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            // Inizializza una nuova sessione se non esiste
            if ((blockEntity instanceof NetworkingBlockEntity device))
            {
                List<String> initialHistory = new ArrayList<>();
                initialHistory.add("Benvenuto nel sistema operativo MCNetworking v1.0");
                initialHistory.add("Digita \"help\" per info");

                return new CacheValue(initialHistory, new ConsoleSession(device));
            }
            else
            {
                throw new IllegalStateException("Il blocco in posizione " + pos + " non è una NetworkingBlockEntity");
            }
        });
    }

    public static void addLine(Level level, BlockPos pos, String line)
    {
        if (line == null || line.isEmpty())
        {
            return;
        }

        List<String> history = getOrCreateSession(level, pos).history;
        history.add(line);

        while (history.size() > MAX_LINES)
        {
            history.removeFirst(); // Rimuove la riga più vecchia
        }
    }

    public static void clearBlock(Level level, BlockPos pos)
    {
        GlobalPos key = GlobalPos.of(level.dimension(), pos);
        cache.remove(key);
    }

    public static void clearAll()
    {
        cache.clear();
    }

    public record CacheValue(List<String> history, ConsoleSession session)
    {
    }
}