package eu.eugenioguidetti.mcnetworking.terminal.command.ip;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 30/07/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.HostBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;
import net.minecraft.network.chat.Component;

/**
 *
 * @author Eugenio Guidetti
 */
public class IpDefaultGatewayCommand implements TerminalCommand
{
    @Override
    public void execute(ConsoleSession session, String[] args) throws IllegalArgumentException, ArrayIndexOutOfBoundsException
    {
        if (!(session.getDevice() instanceof HostBlockEntity host))
        {
            throw new IllegalStateException(String.format(Component
                                                                  .translatable(
                                                                          "mcnetworking.cli.command.cant_assign_default_gateway_to_format")
                                                                  .getString(), session.getDevice().getClass().getSimpleName()));
        }

        Ipv4Address dg;
        dg = new Ipv4Address(args[1]);

        if (dg.getLunghezzaPrefisso() != 32)
        {
            throw new IllegalArgumentException(String.format(Component
                                                                     .translatable("mcnetworking.cli.command.invalid_subnet_mask_format")
                                                                     .getString(), dg.getSubnetMaskString()));
        }

        if (dg.isIndirizzoDiRete() || dg.isIndirizzoDiBroadcast() || dg.isIndirizzoDiLoopback())
        {
            throw new IllegalArgumentException(String.format(Component
                                                                     .translatable("mcnetworking.cli.command.invalid_default_gateway_format")
                                                                     .getString(), dg));
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
        return Component.translatable("mcnetworking.cli.command.description.ip.default_gateway").getString();
    }
}