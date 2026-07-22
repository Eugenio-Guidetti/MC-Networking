package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 10/06/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
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

    public void parseInput(ConsoleSession session, String input)
    {
        if (input == null || input.trim().isEmpty())
        {
            return;
        }

        // Spezza l'input: "ip address 192.168.1.1" -> ["ip", "address", "192.168.1.1"]
        processInput(session, input.trim().split("\\s+"));
    }

    public void processInput(ConsoleSession session, String[] args)
    {
        String commandName = args[0].toLowerCase();

        TerminalCommand command = commands.get(commandName);

        if (command == null || !command.canRunCommand(session))
        {
            session.sendOutput("% Comando sconosciuto: " + commandName);
            return;
        }

        try
        {
            command.execute(session, args);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            session.sendError("Argomento mancante: " + e.getMessage(), e);
        }
        catch (IllegalArgumentException e)
        {
            session.sendError("Argomento invalido: " + e.getMessage(), e);
        }
        catch (IllegalStateException e)
        {
            session.sendError("Stato invalido: " + e.getMessage(), e);
        }
        catch (Exception e)
        {
            session.sendError("Errore generico: " + e.getMessage(), e);

            MCNetworking.LOGGER.error("Errore generico: ", e);
        }
    }
}
