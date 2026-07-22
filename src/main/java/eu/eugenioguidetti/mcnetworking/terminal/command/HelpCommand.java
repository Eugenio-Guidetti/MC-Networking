package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Eugenio Guidetti
 */
public class HelpCommand implements TerminalCommand
{
    Set<Map.Entry<String, TerminalCommand>> entries = null;

    public HelpCommand(Set<Map.Entry<String, TerminalCommand>> entries)
    {
        this.entries = entries;
    }

    @Override
    public void execute(ConsoleSession session, String[] args)
    {
        if (args.length > 1)
        {
            session.sendOutput(showCommandDescription(session, args[1]));
            return;
        }

        List<String> commands = new ArrayList<>();

        for (Map.Entry<String, TerminalCommand> entry : entries)
        {
            if (entry.getValue().canRunCommand(session))
            {
                commands.add(entry.getKey());
            }
        }

        commands.sort(String::compareTo);

        StringBuilder sb = new StringBuilder("Comandi disponibili: ");
        sb.append(commands.getFirst());

        for (int i = 1; i < commands.size(); i++)
        {
            sb.append(", ");
            sb.append(commands.get(i));
        }

        session.sendOutput(sb.toString());
    }

    private String showCommandDescription(ConsoleSession session, String commandName)
    {
        for (Map.Entry<String, TerminalCommand> entry : entries)
        {
            if (entry.getKey().equals(commandName) && entry.getValue().canRunCommand(session))
            {
                return entry.getValue().getDescription(session);
            }
        }

        return "Comando non trovato: " + commandName;
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return true;
    }

    @Override
    public String getDescription(ConsoleSession session)
    {
        return "Mostra i comandi disponibili";
    }
}
