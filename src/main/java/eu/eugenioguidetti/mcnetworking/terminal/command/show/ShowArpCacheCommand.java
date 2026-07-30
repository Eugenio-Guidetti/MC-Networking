package eu.eugenioguidetti.mcnetworking.terminal.command.show;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 30/07/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.HostBlockEntity;
import eu.eugenioguidetti.mcnetworking.block.entity.RouterBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public class ShowArpCacheCommand implements TerminalCommand
{
    @Override
    public void execute(ConsoleSession session, String[] args)
    {
        Map<Ipv4Address, MacAddress> arpCache = null;

        if (session.getDevice() instanceof HostBlockEntity host)
        {
            arpCache = host.getArpCache();
        }
        else if (session.getDevice() instanceof RouterBlockEntity router)
        {
            arpCache = router.getArpCache();
        }

        if (arpCache == null || arpCache.isEmpty())
        {
            session.sendOutput(Component.translatable("mcnetworking.cli.command.show.arp_cache.empty").getString());
            return;
        }

        StringBuilder sb = new StringBuilder();

        for (var entry : arpCache.entrySet())
        {
            sb.append(String.format(Component.translatable("mcnetworking.cli.command.show.arp_cache.row_format").getString(),
                                    entry.getKey(),
                                    entry.getValue()));
        }

        session.sendOutput(String.format(Component.translatable("mcnetworking.cli.command.show.arp_cache.output_format").getString(), sb));
    }

    @Override
    public boolean canRunCommand(ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.PRIV_EXEC) && session.getDevice().getDeviceLayer() >= 3;
    }

    @Override
    public String getDescription(ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.show.arp_cache").getString();
    }
}
