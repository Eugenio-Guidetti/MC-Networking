package eu.eugenioguidetti.mcnetworking.terminal.command.daemons.arpd;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 23/09/2026
 */

import eu.eugenioguidetti.mcnetworking.Utils;
import eu.eugenioguidetti.mcnetworking.block.entity.AbstractL3NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.ArpManager;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.daemons.AbstractDaemonCommand;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 *
 * @author Eugenio Guidetti
 */
public class ArpdCommand extends AbstractDaemonCommand
{
    public ArpdCommand()
    {
        super();

        commands.put("flush", new ArpdFlushCommand());
        commands.put("start", new ArpdStartCommand());
    }

    @Override
    public void execute(@NonNull ConsoleSession session, String @NonNull [] args)
    {
        if (args.length == 1)
        {
            // Mostra l'elenco dei sotto-comandi disponibili se arpd viene chiamato senza argomenti
            session.sendOutput(Utils.listAvailableCommands(new java.util.ArrayList<>(commands.keySet()), "arpd"));
            return;
        }

        int jobId = -1;

        AbstractL3NetworkingBlockEntity netEntity = (AbstractL3NetworkingBlockEntity) session.getDevice();
        Optional<ArpManager> arpManagerOptional = netEntity.getArpManager();

        if (arpManagerOptional.isPresent())
        {
            jobId = arpManagerOptional.get().getJobId();
        }

        // Tolgo il primo argomento "arpd" e aggiungo in fondo il jobId dell' arp manager
        String[] newArgs = new String[args.length];
        if (args.length - 1 >= 0)
        {
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        }
        newArgs[args.length - 1] = Integer.toString(jobId);

        processInput(session, newArgs);
        session.getDevice().sync();
    }

    @Override
    public boolean canRunCommand(@NonNull ConsoleSession session)
    {
        return (session.getCurrentMode().equals(TerminalMode.PRIV_EXEC) || session
                .getCurrentMode()
                .equals(TerminalMode.GLOBAL_CONFIG)) && session.getDevice().getDeviceLayer() >= 3;
    }

    @Override
    public String getDescription(@NonNull ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.arpd").getString();
    }
}
