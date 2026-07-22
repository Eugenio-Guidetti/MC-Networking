package eu.eugenioguidetti.mcnetworking.terminal.command.interfaceConfig;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 10/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.HostBlockEntity;
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
        commands.put("default_gateway", new IpDefaultGatewayCommand());
    }

    @Override
    public void execute(ConsoleSession session, String[] args) throws IllegalArgumentException, ArrayIndexOutOfBoundsException
    {
        if (args.length == 1)
        {
            session.sendOutput(getDescription(session));
            return;
        }

        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
        processInput(session, newArgs);

        session.getDevice().sync();
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return session.getDevice().getDeviceLayer() >= 3;
    }

    @Override
    public String getDescription(ConsoleSession session)
    {
        return "Configurazione IP.";
    }


    static class IpAddressCommand implements TerminalCommand
    {
        @Override
        public void execute(ConsoleSession session, String[] args) throws IllegalArgumentException, ArrayIndexOutOfBoundsException
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

            if (!ip.equals(Ipv4Address.ALL_ZEROS))
            {
                if (ip.getLunghezzaPrefisso() == 0 || ip.getLunghezzaPrefisso() == 32)
                {
                    throw new IllegalArgumentException("Subnet mask non valida: " + ip.getSubnetMaskString());
                }

                if (ip.isIndirizzoDiRete() || ip.isIndirizzoDiBroadcast() || ip.isIndirizzoDiLoopback())
                {
                    throw new IllegalArgumentException("Non puoi assegnare questo indirizzo ad un'interfaccia: " + ip);
                }
            }

            session.getSelectedInterface().setIpAddress(ip);
        }

        @Override
        public boolean canRunCommand(ConsoleSession session)
        {
            return session.getCurrentMode().equals(TerminalMode.INTERFACE_CONFIG);
        }

        @Override
        public String getDescription(ConsoleSession session)
        {
            return "Assegna un indirizzo IP all'interfaccia";
        }
    }

    static class IpDefaultGatewayCommand implements TerminalCommand
    {
        @Override
        public void execute(ConsoleSession session, String[] args) throws IllegalArgumentException, ArrayIndexOutOfBoundsException
        {
            if (!(session.getDevice() instanceof HostBlockEntity host))
            {
                throw new IllegalStateException("Non puoi assegnare un default gateway ad un " + session
                        .getDevice()
                        .getClass()
                        .getSimpleName());
            }

            Ipv4Address dg;
            dg = new Ipv4Address(args[1]);

            if (dg.getLunghezzaPrefisso() != 32)
            {
                throw new IllegalArgumentException("Subnet mask non valida: " + dg.getSubnetMaskString());
            }

            if (dg.isIndirizzoDiRete() || dg.isIndirizzoDiBroadcast() || dg.isIndirizzoDiLoopback())
            {
                throw new IllegalArgumentException("Default gateway non valido: " + dg);
            }

            host.setDefaultGateway(dg);
        }

        @Override
        public boolean canRunCommand(ConsoleSession session)
        {
            return session.getCurrentMode().equals(TerminalMode.GLOBAL_CONFIG) && session.getDevice() instanceof HostBlockEntity;
        }

        @Override
        public String getDescription(ConsoleSession session)
        {
            return "Imposta l'indirizzo IPv4 del default gateway";
        }
    }
}
