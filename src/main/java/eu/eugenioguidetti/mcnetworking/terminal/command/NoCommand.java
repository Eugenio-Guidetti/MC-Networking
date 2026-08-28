package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 27/08/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

/**
 *
 * @author Eugenio Guidetti
 */
public class NoCommand implements TerminalCommand
{
    private final CommandRegistrar rootRegistrar;

    public NoCommand(CommandRegistrar rootRegistrar)
    {
        this.rootRegistrar = rootRegistrar;
    }

    @Override
    public void execute(@NonNull ConsoleSession session, String @NonNull [] args)
    {
        if (args.length < 2)
        {
            throw new IndexOutOfBoundsException();
        }

        // Rimuoviamo "no" dall' array e passiamo tutto al motore di annullamento
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        rootRegistrar.processUndo(session, newArgs);
    }

    @Override
    public boolean canRunCommand(@NonNull ConsoleSession session)
    {
        return true;
    }

    @Override
    public String getDescription(@NonNull ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.no").getString();
    }
}
