package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 10/06/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.*;

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

    public final void parseInput(ConsoleSession session, String input)
    {
        if (input == null || input.trim().isEmpty())
        {
            session.updateOutput();
            return;
        }

        processInput(session, input.trim().split("\\s+"));
    }

    public static @NonNull List<String> matchCommands(ConsoleSession session,
                                                      @NonNull Set<Map.Entry<String, TerminalCommand>> entries,
                                                      String partialCommandName)
    {
        List<String> matchedCommands = new ArrayList<>();
        String matchedCommandName = null;
        int matchCount = 0;

        for (var entry : entries)
        {
            if (!entry.getKey().startsWith(partialCommandName) || !entry.getValue().canRunCommand(session))
            {
                continue;
            }

            // Match trovato
            matchedCommands.add(entry.getKey());

            // L'utente ha digitato il nome completo del comando
            if (entry.getKey().equals(partialCommandName))
            {
                matchedCommands.clear();
                matchedCommands.add(partialCommandName);
                break;
            }
        }

        return matchedCommands;
    }

    public final void processInput(ConsoleSession session, String @NonNull [] args)
    {
        String partialCommandName = args[0].toLowerCase();
        List<String> matchedCommands = matchCommands(session, commands.entrySet(), partialCommandName);
        int matchCount = matchedCommands.size();

        if (matchCount == 0)
        {
            if (this instanceof TerminalCommand)
            {
                session.sendOutput(String.format(Component.translatable("mcnetworking.cli.incomplete_command_format").getString(),
                                                 partialCommandName));
            }
            else
            {
                session.sendOutput(String.format(Component.translatable("mcnetworking.cli.unknown_command_format").getString(),
                                                 partialCommandName));
            }

            return;
        }
        else if (matchCount > 1)
        {
            session.sendOutput(String.format(Component.translatable("mcnetworking.cli.ambiguous_command_format").getString(),
                                             partialCommandName));
            return;
        }

        String matchedCommandName = matchedCommands.getFirst();

        TerminalCommand command = commands.get(matchedCommandName);

        // Sovrascrivo il nome parziale del comando (args[0]) con il nome esteso.
        args[0] = matchedCommandName;

        try
        {
            command.execute(session, args);
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            session.sendError(String.format(Component.translatable("mcnetworking.cli.missing_argument_error_format").getString(),
                                            matchedCommandName));
        }
        catch (IllegalArgumentException e)
        {
            session.sendError(String.format(Component.translatable("mcnetworking.cli.invalid_argument_error_format").getString(),
                                            matchedCommandName), e);
        }
        catch (IllegalStateException e)
        {
            session.sendError(String.format(Component.translatable("mcnetworking.cli.invalid_state_error_format").getString(),
                                            matchedCommandName), e);
        }
        catch (Exception e)
        {
            session.sendError(String.format(Component.translatable("mcnetworking.cli.generic_error_format").getString(),
                                            matchedCommandName), e);

            MCNetworking.LOGGER.error("Errore generico: ", e);
        }
    }

    public final @NonNull List<String> autocompleteCommand(ConsoleSession session, String draftCommand)
    {
        if (draftCommand == null)
        {
            draftCommand = "";
        }

        // Se la stringa finisce con uno spazio, significa che l'utente vuole i suggerimenti per la parola successiva
        boolean endsWithSpace = draftCommand.endsWith(" ");
        String[] parti = draftCommand.trim().split("\\s+");

        if (endsWithSpace && !draftCommand.trim().isEmpty())
        {
            // Aggiungiamo un elemento vuoto per forzare la ricerca dei sotto comandi
            parti = Arrays.copyOf(parti, parti.length + 1);
            parti[parti.length - 1] = "";
        }
        else if (draftCommand.trim().isEmpty())
        {
            parti = new String[]{""};
        }

        return autocompleteCommand(session, parti);
    }

    public final @NonNull List<String> autocompleteCommand(ConsoleSession session, String[] parti)
    {
        List<String> availableCommands = new java.util.ArrayList<>(List.copyOf(commands.keySet()));

        availableCommands.removeIf(s -> !commands.get(s).canRunCommand(session));

        if (parti == null || parti.length == 0)
        {
            return availableCommands;
        }

        availableCommands.removeIf(s -> !s.startsWith(parti[0]));

        if (availableCommands.size() == 1)
        {
            String comando = availableCommands.getFirst();

            if (commands.get(comando) instanceof CommandRegistrar registrar)
            {

                // SCENDI NEL SOTTOMENU SOLO SE:
                // 1. L'utente ha digitato lo spazio (l'array ha più di 1 elemento)
                // OPPURE
                // 2. L'utente ha digitato esattamente la parola intera (es. "ip" e non "i")
                if (parti.length > 1 || parti[0].equals(comando))
                {
                    availableCommands = registrar.autocompleteCommand(session, Arrays.copyOfRange(parti, 1, parti.length));

                    availableCommands.removeIf(s -> s.equals("help"));

                    for (int i = 0; i < availableCommands.size(); i++)
                    {
                        availableCommands.set(i, comando + " " + availableCommands.get(i));
                    }
                }
            }
        }

        return availableCommands;
    }
}
