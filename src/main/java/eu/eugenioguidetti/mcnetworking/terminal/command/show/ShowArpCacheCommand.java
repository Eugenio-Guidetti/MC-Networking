package eu.eugenioguidetti.mcnetworking.terminal.command.show;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 30/07/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.AbstractL3NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.ArpManager;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalMode;
import eu.eugenioguidetti.mcnetworking.terminal.command.TerminalCommand;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Optional;

/**
 *
 * @author Eugenio Guidetti
 */
public class ShowArpCacheCommand implements TerminalCommand
{
    @Override
    public void execute(@NonNull ConsoleSession session, String @NonNull [] args)
    {
        Map<Ipv4Address, ArpManager.ArpCacheEntry> arpCache = null;

        AbstractL3NetworkingBlockEntity l3NetEntity = (AbstractL3NetworkingBlockEntity) session.getDevice();
        Optional<ArpManager> optionalArpManager = l3NetEntity.getArpManager();
        if (optionalArpManager.isEmpty())
        {
            session.sendError(String.format(Component.translatable("mcnetworking.cli.command.jobs.daemon_not_running_format").getString(),
                                            ArpManager.class.getSimpleName()));

            return;
        }

        arpCache = optionalArpManager.get().getArpCache();

        if (arpCache == null || arpCache.isEmpty())
        {
            session.sendOutput(Component.translatable("mcnetworking.cli.command.show.arp_cache.empty").getString());
            return;
        }

        StringBuilder sb = new StringBuilder();

        for (var entry : arpCache.entrySet())
        {
            String ttl = entry.getValue().getTicksRemaining() == -1 ? Component
                                                                      .translatable("mcnetworking.cli.command.show.arp_cache.static")
                                                                      .getString() : Integer.toString(entry.getValue().getTicksRemaining());

            sb.append(String.format(Component.translatable("mcnetworking.cli.command.show.arp_cache.row_format").getString(),
                                    entry.getKey(),
                                    entry.getValue().getTargetMac(),
                                    ttl));
        }

        session.sendOutput(String.format(Component.translatable("mcnetworking.cli.command.show.arp_cache.output_format").getString(), sb));
    }

    @Override
    public boolean canRunCommand(@NonNull ConsoleSession session)
    {
        return session.getCurrentMode().equals(TerminalMode.PRIV_EXEC) && session.getDevice().getDeviceLayer() >= 3;
    }

    @Override
    public String getDescription(@NonNull ConsoleSession session)
    {
        return Component.translatable("mcnetworking.cli.command.description.show.arp_cache").getString();
    }
}
