package eu.eugenioguidetti.mcnetworking.terminal.gui;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 16/06/2026
 */

import net.minecraft.core.GlobalPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public class ClientCommandHistoryCache
{
    private static final Map<GlobalPos, List<String>> commandHistory = new HashMap<>();

    public static void addCommand(GlobalPos pos, String command)
    {
        if (command == null || command.isEmpty())
        {
            return;
        }

        if (!commandHistory.containsKey(pos))
        {
            commandHistory.put(pos, new ArrayList<>());
        }

        if (!commandHistory.get(pos).isEmpty() && command.equals(commandHistory.get(pos).getLast()))
        {
            return;
        }

        commandHistory.get(pos).add(command);
    }

    public static @NotNull String getCommand(GlobalPos pos, int index)
    {
        if (!commandHistory.containsKey(pos))
        {
            return "";
        }

        if (index < 0 || index >= commandHistory.get(pos).size())
        {
            return "";
        }

        return commandHistory.get(pos).get(index);
    }

    public static int getHistorySize(GlobalPos pos)
    {
        if (!commandHistory.containsKey(pos))
        {
            return 0;
        }

        return commandHistory.get(pos).size();
    }

    public static void clearCache(GlobalPos pos)
    {
        commandHistory.remove(pos);
    }

    public static void clearAll()
    {
        commandHistory.clear();
    }
}
