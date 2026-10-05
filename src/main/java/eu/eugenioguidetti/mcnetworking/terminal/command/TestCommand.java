package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 17/08/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.HostBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.l4.TestClient;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.l4.TestServer;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.TransportProtocol;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class TestCommand implements TerminalCommand
{
    @Override
    public void execute(@NonNull ConsoleSession session, String @NonNull [] args)
    {
        if (!(session.getDevice() instanceof HostBlockEntity l4netEntity))
        {
            return;
        }

        int porta;
        if (args.length >= 3)
        {
            porta = Integer.parseInt(args[2]);
        }
        else
        {
            porta = 80;
        }

        if (args[1].equals("client"))
        {
            session.getDevice().startJob(new TestClient(l4netEntity, TransportProtocol.UDP, porta), true);
        }
        else if (args[1].equals("server"))
        {
            session.getDevice().startJob(new TestServer(l4netEntity, TransportProtocol.UDP, porta), false);
        }
    }

    @Override
    public boolean canRunCommand(@NonNull ConsoleSession session)
    {
        return session.getDevice() instanceof HostBlockEntity;
    }

    @Override
    public String getDescription(@NonNull ConsoleSession session)
    {
        return "test <client | server> [porta] Avvia un client/server l4 di test\n";
    }
}
