package eu.eugenioguidetti.mcnetworking.terminal.command.ip;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 30/07/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.AbstractL3NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4CidrAddress;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.UndoableTerminalCommand;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class IpAddressCommand implements UndoableTerminalCommand
{
    @Override
    public void execute(@NonNull ConsoleSession session,
                        String @NonNull [] args) throws IllegalArgumentException, ArrayIndexOutOfBoundsException
    {
        if (!(session.getDevice() instanceof AbstractL3NetworkingBlockEntity l3NetEntity))
        {
            return;
        }

        Ipv4CidrAddress newIp;

        if (args.length == 2)
        {
            // a.b.c.d/sn
            newIp = new Ipv4CidrAddress(args[1]);
        }
        else
        {
            // a.b.c.d x.y.z.w
            newIp = new Ipv4CidrAddress(args[1], args[2]);
        }

        if (!newIp.equals(Ipv4CidrAddress.ALL_ZEROS))
        {
            if (newIp.subnetMask().lunghezzaPrefisso() == 0 || newIp.subnetMask().lunghezzaPrefisso() >= 31)
            {
                // L'indirizzo cidr di un'interfaccia fisica deve avere una lunghezza di prefisso inclusa nell'intervallo [1; 30]

                throw new IllegalArgumentException(String.format(Component
                                                                         .translatable("mcnetworking.cli.command.invalid_subnet_mask_format")
                                                                         .getString(), newIp.subnetMask()));
            }

            if (newIp.isIndirizzoDiRete() || newIp.isIndirizzoDiBroadcast() || newIp.isLoopback())
            {
                // L'indirizzo cidr di un'interfaccia fisica non può essere un indirizzo di rete, di broadcast o di loopback (gestito tramite l'interfaccia virtuale "lo")

                throw new IllegalArgumentException(String.format(Component
                                                                         .translatable(
                                                                                 "mcnetworking.cli.command.invalid_interface_ip_address_format")
                                                                         .getString(), newIp));
            }

            for (NetworkInterface nic : session.getDevice().getNics().values())
            {
                if (nic.isLoopback() || nic.getIpAddress().equals(Ipv4CidrAddress.ALL_ZEROS))
                {
                    continue;
                }

                if (nic.getName().equals(session.getSelectedInterfaceName()))
                {
                    continue;
                }

                if (Ipv4CidrAddress.retiSovrapposte(newIp, nic.getIpAddress()))
                {
                    throw new IllegalArgumentException(Component
                                                               .translatable("mcnetworking.cli.command.network_already_connected")
                                                               .getString());
                }
            }
        }

        session.getSelectedInterface().setIpAddress(newIp);

        l3NetEntity.sendGratuitousArpRequest(session.getSelectedInterface().getName());
    }

    @Override
    public boolean canRunCommand(@NonNull ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.INTERFACE_CONFIG);
    }

    @Override
    public String getDescription(@NonNull ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.ip.address").getString();
    }

    @Override
    public void undo(@NonNull ConsoleSession session, String @NonNull [] args)
    {
        if (!(session.getDevice() instanceof AbstractL3NetworkingBlockEntity l3NetEntity))
        {
            return;
        }

        session.getSelectedInterface().setIpAddress(Ipv4CidrAddress.ALL_ZEROS);

        l3NetEntity.sendGratuitousArpRequest(session.getSelectedInterface().getName());
    }
}