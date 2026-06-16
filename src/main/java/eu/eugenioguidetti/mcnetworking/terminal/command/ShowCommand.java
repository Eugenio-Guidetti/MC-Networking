package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 09/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.HostBlockEntity;
import eu.eugenioguidetti.mcnetworking.block.entity.SwitchBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;

/**
 *
 * @author Eugenio Guidetti
 */
public class ShowCommand implements TerminalCommand
{
    @Override
    public String execute(ConsoleSession session, String[] args) throws ArrayIndexOutOfBoundsException
    {
        if (session.getCurrentMode().equals(TerminalMode.INTERFACE_CONFIG))
        {
            return executeInterface(session, args, session.getSelectedInterface());
        }

        return executePrivExec(session, args);
    }

    private String executeInterface(ConsoleSession session,
                                    String[] args,
                                    NetworkInterface selectedInterface) throws ArrayIndexOutOfBoundsException
    {
        String message = null;

        switch (args[1])
        {
            case "mac":
                message = selectedInterface.getMacAddress().toString();
                break;

            case "ip":
                message = selectedInterface.getIpAddress().toString();
                break;

            default:
                message = "Nessuna informazione disponibile per: " + args[1];
        }

        return message;
    }

    private String executePrivExec(ConsoleSession session, String[] args) throws ArrayIndexOutOfBoundsException, IllegalArgumentException
    {
        String message = null;
        StringBuilder sb = new StringBuilder(args[1]);

        for (int i = 2; i < args.length; i++)
        {
            sb.append(" ");
            sb.append(args[i]);
        }

        switch (sb.toString())
        {
            case "pos":
                message = session.getDevice().getBlockPos().toShortString();
                break;

            case "arp":
                if (!(session.getDevice() instanceof HostBlockEntity hostBlock))
                {
                    throw new IllegalArgumentException("Operazione non valida per " + session.getDevice().getClass().getSimpleName());
                }

                if (hostBlock.getArpCache().isEmpty())
                {
                    message = "Arp cache vuota";
                    break;
                }

                message = hostBlock.getArpCache().toString();
                break;

            case "switching table":
                if (!(session.getDevice() instanceof SwitchBlockEntity switchBlockEntity))
                {
                    throw new IllegalArgumentException("Operazione non valida per " + session.getDevice().getClass().getSimpleName());
                }

                if (switchBlockEntity.getSwitchingTable().isEmpty())
                {
                    message = "Switching table vuota";
                    break;
                }

                message = switchBlockEntity.getSwitchingTable().toString();
                break;

            default:
                message = "Nessuna informazione disponibile per: " + sb;
        }

        return message;
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.INTERFACE_CONFIG) || session.getCurrentMode().equals(TerminalMode.PRIV_EXEC);
    }

    @Override
    public String getDescription()
    {
        return "Ottieni informazioni sull'interfaccia";
    }
}
