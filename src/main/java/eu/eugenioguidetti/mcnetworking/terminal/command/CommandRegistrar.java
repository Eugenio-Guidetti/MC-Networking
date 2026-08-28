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

    public @NonNull List<String> matchCommands(ConsoleSession session, String partialCommandName)
    {
        Set<Map.Entry<String, TerminalCommand>> entries = commands.entrySet();
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
        dispatchInput(session, args, false);
    }

    public final void processUndo(ConsoleSession session, String @NonNull [] args)
    {
        dispatchInput(session, args, true);
    }

    private void dispatchInput(ConsoleSession session, String @NonNull [] args, boolean isUndo)
    {
        String partialCommandName = args[0].toLowerCase();
        List<String> matchedCommands = matchCommands(session, partialCommandName);
        int matchCount = matchedCommands.size();

        if (matchCount == 0)
        {
            session.sendOutput(String.format(Component.translatable("mcnetworking.cli.unknown_command_format").getString(),
                                             partialCommandName));

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
            if (isUndo)
            {
                if (command instanceof UndoableTerminalCommand undoable)
                {
                    undoable.undo(session, args);
                }
                else
                {
                    session.sendError(String.format(Component.translatable("mcnetworking.cli.cannot_undo_command_format").getString(),
                                                    matchedCommandName));
                }
            }
            else
            {
                if (command instanceof CommandRegistrar && args.length == 1)
                {
                    session.sendOutput(String.format(Component.translatable("mcnetworking.cli.incomplete_command_format").getString(),
                                                     partialCommandName));
                }
                else
                {
                    command.execute(session, args);
                }
            }
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

        return autocompleteCommand(session, parti, false, false);
    }

    public final @NonNull List<String> autocompleteCommand(ConsoleSession session, String[] parti, boolean isHelp, boolean isUndo)
    {
        if (isHelp && isUndo)
        {
            return new ArrayList<>();
        }

        List<String> availableCommands = new ArrayList<>(List.copyOf(commands.keySet()));

        // Rimuovi i comandi che non possono essere eseguiti
        availableCommands.removeIf(s ->
                                   {
                                       TerminalCommand cmd = commands.get(s);
                                       if (!cmd.canRunCommand(session))
                                       {
                                           return true;
                                       }
                                       if (isHelp && s.equals("help"))
                                       {
                                           return true;
                                       }
                                       if (isUndo && !(cmd instanceof UndoableTerminalCommand))
                                       {
                                           return true;
                                       }
                                       return false;
                                   });

        // Elenco tutti i comandi quando auto-completo una stringa vuota
        if (parti == null || parti.length == 0)
        {
            return availableCommands;
        }

        // Elenco di comandi che iniziano con la prima parola dell'utente
        availableCommands.removeIf(s -> !s.startsWith(parti[0]));
        if (availableCommands.size() != 1)
        {
            return availableCommands;
        }

        // Auto-completo un comando semplice o solo l'inizio di un comando composto
        String comando = availableCommands.getFirst();

        // L'utente non ha ancora completato la parola attuale
        if (parti.length == 1 && !parti[0].equals(comando))
        {
            return availableCommands;
        }

        TerminalCommand foundCommand = commands.get(comando);

        // Risoluzione dei sotto-comandi in un unico blocco
        List<String> subCommands;

        if (foundCommand instanceof CommandRegistrar registrar)
        {
            subCommands = registrar.autocompleteCommand(session, Arrays.copyOfRange(parti, 1, parti.length), isHelp, isUndo);
        }
        else if (comando.equals("help"))
        {
            subCommands = this.autocompleteCommand(session, Arrays.copyOfRange(parti, 1, parti.length), true, isUndo);
        }
        else if (comando.equals("no"))
        {
            subCommands = this.autocompleteCommand(session, Arrays.copyOfRange(parti, 1, parti.length), isHelp, true);
        }
        else
        {
            // Se non ha sottocomandi delegabili, restituiamo il comando base
            return availableCommands;
        }

        // Formattazione e ricomposizione finale (comando_base + sotto_comandi)
        subCommands.replaceAll(s -> comando + " " + s);

        return subCommands.isEmpty() ? availableCommands : subCommands;
    }

    public TerminalCommand getCommand(String name, @NonNull ConsoleSession session)
    {
        TerminalCommand command = commands.get(name);

        if (command != null && command.canRunCommand(session))
        {
            return command;
        }

        return null;
    }
}
