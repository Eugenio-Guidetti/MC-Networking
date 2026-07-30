package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 10/06/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import net.minecraft.network.chat.Component;

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

        processInput(session, input.trim().split("\\s+"));
    }

    public void processInput(ConsoleSession session, String[] args)
    {
        String commandName = args[0].toLowerCase();

        TerminalCommand command = commands.get(commandName);

        if (command == null || !command.canRunCommand(session))
        {
            session.sendOutput(String.format(Component.translatable("mcnetworking.cli.unknown_command_format").getString(), commandName));
            return;
        }

        try
        {
            command.execute(session, args);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            session.sendError(String.format(Component.translatable("mcnetworking.cli.missing_argument_error_format").getString(),
                                            commandName), e);
        }
        catch (IllegalArgumentException e)
        {
            session.sendError(String.format(Component.translatable("mcnetworking.cli.invalid_argument_error_format").getString(),
                                            commandName), e);
        }
        catch (IllegalStateException e)
        {
            session.sendError(String.format(Component.translatable("mcnetworking.cli.invalid_state_error_format").getString(), commandName),
                              e);
        }
        catch (Exception e)
        {
            session.sendError(String.format(Component.translatable("mcnetworking.cli.generic_error_format").getString(), commandName), e);

            MCNetworking.LOGGER.error("Errore generico: ", e);
        }
    }
}
