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
    public void execute(@NonNull ConsoleSession session, String @NonNull [] args)
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

        List<String> commands = CommandRegistrar.matchCommands(session, entries, draftCommand);

        String output;

        if (commands.size() == 1)
        {
            output = showCommandDescription(session, commands.getFirst());
        }
        else
        {
            output = Utils.listAvailableCommands(commands, draftCommand);
        }

        session.sendOutput(output);
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

        return String.format(Component.translatable("mcnetworking.cli.unknown_command_format").getString(), commandName);
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
