package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 10/06/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public abstract class CommandRegistrar
{
    protected final Map<String, TerminalCommand> commands;

    public CommandRegistrar()
    {
        commands = new HashMap<>();

        commands.put("help", new HelpCommand(commands.entrySet()));
    }

    public String parseInput(ConsoleSession session, String input)
    {
        if (input == null || input.trim().isEmpty())
        {
            return "";
        }

        // Spezza l'input: "ip address 192.168.1.1" -> ["ip", "address", "192.168.1.1"]
        return processInput(session, input.trim().split("\\s+"));
    }

    public String processInput(ConsoleSession session, String[] args)
    {
        String commandName = args[0].toLowerCase();

        TerminalCommand command = commands.get(commandName);

        if (command == null || !command.canRunCommand(session))
        {
            return "% Comando sconosciuto: " + commandName;
        }

        String error = "% Errore: ";

        try
        {
            return command.execute(session, args);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            error += "Argomento mancante: " + e.getMessage();
        }
        catch (IllegalArgumentException e)
        {
            error += "Argomento invalido: " + e.getMessage();
        }
        catch (IllegalStateException e)
        {
            error += "Stato invalido: " + e.getMessage();
        }
        catch (Exception e)
        {
            error += "Errore generico:" + e.getMessage();
        }

        return error;
    }
}
