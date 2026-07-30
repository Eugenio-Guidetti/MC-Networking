package eu.eugenioguidetti.mcnetworking.terminal.command.ip;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 30/07/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;
import net.minecraft.network.chat.Component;

/**
 *
 * @author Eugenio Guidetti
 */
public class IpAddressCommand implements TerminalCommand
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
                throw new IllegalArgumentException(String.format(Component
                                                                         .translatable("mcnetworking.cli.command.invalid_subnet_mask_format")
                                                                         .getString(), ip.getSubnetMaskString()));
            }

            if (ip.isIndirizzoDiRete() || ip.isIndirizzoDiBroadcast() || ip.isIndirizzoDiLoopback())
            {
                throw new IllegalArgumentException(String.format(Component
                                                                         .translatable(
                                                                                 "mcnetworking.cli.command.invalid_interface_ip_address_format")
                                                                         .getString(), ip));
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
        return Component.translatable("mcnetworking.cli.command.description.ip.address").getString();
    }
}