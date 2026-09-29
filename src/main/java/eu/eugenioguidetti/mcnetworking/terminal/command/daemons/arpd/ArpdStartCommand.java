package eu.eugenioguidetti.mcnetworking.terminal.command.daemons.arpd;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 29/09/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.AbstractL3NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.ArpManager;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class ArpdStartCommand implements TerminalCommand
{
    @Override
    public void execute(@NonNull ConsoleSession session, String @NonNull [] args)
    {
        AbstractL3NetworkingBlockEntity netEntity = (AbstractL3NetworkingBlockEntity) session.getDevice();

        if (netEntity.getArpManager().isPresent())
        {
            throw new IllegalStateException(String.format(Component
                                                                  .translatable("mcnetworking.cli.command.jobs.already_running_format")
                                                                  .getString(), ArpManager.class.getSimpleName()));
        }

        netEntity.startArpManager();
    }

    @Override
    public boolean canRunCommand(@NonNull ConsoleSession session)
    {
        return true;
    }

    @Override
    public String getDescription(@NonNull ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.arpd.start").getString();
    }
}
