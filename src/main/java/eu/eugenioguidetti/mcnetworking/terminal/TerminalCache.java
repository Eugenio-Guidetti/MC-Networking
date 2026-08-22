package eu.eugenioguidetti.mcnetworking.terminal;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 07/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

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
    // Cache server-side

    private static final int MAX_LINES = 150;

    // Ogni device ha una propria cache contenente: le ultime 150 righe di output, ConsoleSession con: pos, prompt attuale, eventuale interfaccia selezionata
    private static final Map<GlobalPos, CacheValue> CACHES = new HashMap<>();

    public static CacheValue getOrCreateSession(@NotNull NetworkingBlockEntity entity)
    {
        ServerLevel level = (ServerLevel) entity.getLevel();
        BlockPos pos = entity.getBlockPos();

        return getOrCreateSession(level, pos);
    }

    public static CacheValue getOrCreateSession(@NotNull ServerLevel level, BlockPos pos)
    {
        if (level.isClientSide())
        {
            throw new IllegalStateException("Il Client sta cercando di accedere alla cache del Server");
        }

        // Creiamo la chiave univoca unendo la dimensione attuale e le coordinate
        GlobalPos key = GlobalPos.of(level.dimension(), pos);

        return CACHES.computeIfAbsent(key, k ->
        {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            // Inizializza una nuova sessione se non esiste
            if ((blockEntity instanceof NetworkingBlockEntity device))
            {
                List<String> initialHistory = new ArrayList<>();
                initialHistory.add("");

                String welcomeMsg = Component.translatable("mcnetworking.cli.welcome_message").getString();

                for (int i = 0; i < welcomeMsg.length(); i++)
                {
                    char c = welcomeMsg.charAt(i);

                    if (c == '\n')
                    {
                        initialHistory.add("");
                    }
                    else if (c == '\r')
                    {
                        initialHistory.set(initialHistory.size() - 1, "");
                    }
                    else
                    {
                        String currentLine = initialHistory.getLast();
                        initialHistory.set(initialHistory.size() - 1, currentLine + c);
                    }
                }

                return new CacheValue(initialHistory, new ConsoleSession(pos, level, device));
            }
            else
            {
                throw new IllegalStateException("Il blocco in posizione " + pos + " non è una NetworkingBlockEntity");
            }
        });
    }

    public static void renderOutput(ServerLevel level, BlockPos pos, String text)
    {
        if (text == null || text.isEmpty())
        {
            return;
        }

        List<String> history = getOrCreateSession(level, pos).history();

        if (history.isEmpty())
        {
            history.add("");
        }

        for (int i = 0; i < text.length(); i++)
        {
            char c = text.charAt(i);

            if (c == '\n')
            {
                history.add("");
            }
            else if (c == '\t')
            {
                String currentLine = history.getLast();
                currentLine += " ".repeat(4 - (currentLine.length() % 4));
                history.set(history.size() - 1, currentLine);
            }
            else if (c == '\r')
            {
                history.set(history.size() - 1, "");
            }
            else
            {
                String currentLine = history.getLast();
                history.set(history.size() - 1, currentLine + c);
            }
        }

        while (history.size() > MAX_LINES)
        {
            history.removeFirst();
        }
    }

    public static void clearBlock(@NotNull Level level, BlockPos pos)
    {
        GlobalPos key = GlobalPos.of(level.dimension(), pos);
        CACHES.get(key).history.clear();
    }

    public static void removeBlock(@NotNull Level level, BlockPos pos)
    {
        GlobalPos key = GlobalPos.of(level.dimension(), pos);
        CACHES.remove(key);
    }

    public static void clearAll()
    {
        CACHES.clear();
    }

    public record CacheValue(List<String> history, ConsoleSession session)
    {
    }
}