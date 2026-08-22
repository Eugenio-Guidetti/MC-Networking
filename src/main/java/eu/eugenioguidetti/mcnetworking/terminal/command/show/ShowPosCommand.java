package eu.eugenioguidetti.mcnetworking.terminal.command.show;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 30/07/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class ShowPosCommand implements TerminalCommand
{
    @Override
    public void execute(@NonNull ConsoleSession session, String @NonNull [] args)
    {
        session.sendOutput(String.format(Component.translatable("mcnetworking.cli.command.show.pos.output_format").getString(),
                                         session.getDevice().getLevel().dimension().identifier(),
                                         session.getDevice().getBlockPos().toShortString()));
    }

    @Override
    public boolean canRunCommand(@NonNull ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.PRIV_EXEC);
    }

    @Override
    public String getDescription(@NonNull ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.show.pos").getString();
    }
}
