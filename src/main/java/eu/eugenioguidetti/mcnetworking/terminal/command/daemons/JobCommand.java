package eu.eugenioguidetti.mcnetworking.terminal.command.daemons;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 29/09/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

/**
 *
 * @author Eugenio Guidetti
 */
public class JobCommand extends AbstractDaemonCommand
{
    @Override
    public void execute(@NonNull ConsoleSession session, String @NonNull [] args)
    {
        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        processInput(session, newArgs);

        session.getDevice().sync();
    }

    @Override
    public boolean canRunCommand(@NonNull ConsoleSession session)
    {
        return (session.getCurrentMode().equals(TerminalMode.PRIV_EXEC) || session.getCurrentMode().equals(TerminalMode.GLOBAL_CONFIG));
    }

    @Override
    public String getDescription(@NonNull ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.job").getString();
    }
}
