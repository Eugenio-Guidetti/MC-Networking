package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
 */

import eu.eugenioguidetti.mcnetworking.Utils;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Eugenio Guidetti
 */
public class HelpCommand implements TerminalCommand
{
    private final CommandRegistrar registrar;

    public HelpCommand(CommandRegistrar registrar)
    {
        this.registrar = registrar;
    }

    @Override
    public void execute(@NonNull ConsoleSession session, String @NonNull [] args)
    {
        executeCommand(session, this.registrar, args);
    }

    private void executeCommand(@NonNull ConsoleSession session, CommandRegistrar registrar, String @NonNull [] args)
    {
        String draftCommand;
        if (args.length <= 1)
        {
            draftCommand = "";
        }
        else
        {
            draftCommand = args[1];
        }

        List<String> matchedCommands = registrar.matchCommands(session, draftCommand);

        String output;
        if (matchedCommands.size() == 1)
        {
            TerminalCommand command = registrar.getCommand(matchedCommands.getFirst(), session);

            if (command == null)
            {
                output = String.format(Component.translatable("mcnetworking.cli.unknown_command_format").getString(),
                                       matchedCommands.getFirst());
                return;
            }

            if (command instanceof CommandRegistrar nestedRegistrar && args.length > 2)
            {
                String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
                newArgs[0] = args[0];

                executeCommand(session, nestedRegistrar, newArgs);

                return;
            }
            else
            {
                output = command.getDescription(session);
            }
        }
        else
        {
            output = Utils.listAvailableCommands(matchedCommands, draftCommand);
        }

        session.sendOutput(output);
    }

    @Override
    public boolean canRunCommand(@NonNull ConsoleSession session)
    {
        return true;
    }

    @Override
    public String getDescription(@NonNull ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.help").getString();
    }
}
