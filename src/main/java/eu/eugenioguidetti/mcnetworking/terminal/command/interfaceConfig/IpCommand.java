package eu.eugenioguidetti.mcnetworking.terminal.command.interfaceConfig;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 10/06/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.CommandRegistrar;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;

import java.util.Arrays;

/**
 *
 * @author Eugenio Guidetti
 */
public class IpCommand extends CommandRegistrar implements TerminalCommand
{
    public IpCommand()
    {
        super();

        commands.put("address", new IpAddressCommand());
    }

    @Override
    public String execute(ConsoleSession session, String[] args) throws IllegalArgumentException, ArrayIndexOutOfBoundsException
    {
        if (args.length == 1)
        {
            return getDescription();
        }

        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        String output = processInput(session, newArgs);

        session.getDevice().sync();
        return output;
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.INTERFACE_CONFIG) && session.getDevice().getDeviceLayer() >= 3;
    }

    @Override
    public String getDescription()
    {
        return "Configurazione IP.";
    }


    static class IpAddressCommand implements TerminalCommand
    {
        @Override
        public String execute(ConsoleSession session, String[] args) throws IllegalArgumentException, ArrayIndexOutOfBoundsException
        {
            Ipv4Address ip;

            if (args.length == 2)
            {
                ip = new Ipv4Address(args[1]);
            }
            else
            {
                ip = new Ipv4Address(args[1], args[2]);
            }

            if (ip.getLunghezzaPrefisso() == 0 || ip.getLunghezzaPrefisso() == 32)
            {
                throw new IllegalArgumentException("Subnet mask non valida: " + ip.getSubnetMaskString());
            }

            if (ip.isIndirizzoDiRete() || ip.isIndirizzoDiBroadcast() || ip.isIndirizzoDiLoopback())
            {
                throw new IllegalArgumentException("Non puoi assegnare questo indirizzo ad un'interfaccia: " + ip);
            }

            session.getSelectedInterface().setIpAddress(ip);

            return "";
        }

        @Override
        public boolean canRunCommand(ConsoleSession session)
        {
            return true;
        }

        @Override
        public String getDescription()
        {
            return "Assegna un indirizzo IP all'interfaccia";
        }
    }
}
