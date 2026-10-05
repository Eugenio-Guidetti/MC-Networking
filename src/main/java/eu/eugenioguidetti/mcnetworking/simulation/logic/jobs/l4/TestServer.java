package eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.l4;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 03/10/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.HostBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.ApplicationPayload;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.TransportProtocol;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.TransportSegment;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class TestServer extends L4Job
{
    public TestServer(@NonNull HostBlockEntity l4netEntity, TransportProtocol protocol, int port)
    {
        super(l4netEntity, protocol, port);
    }

    @Override
    protected void onL4Start()
    {
        super.onL4Start();

        session.sendOutput("Server avviato sulla porta: " + getLocalPort() + "\n");
    }

    @Override
    public void onSegmentReceived(@NonNull TransportSegment segment, @NonNull Ipv4Address sourceIp)
    {
        session.sendOutput("Rispondo a: " + segment.payload().getDisplayString() + "\n");

        ApplicationPayload payload = new ApplicationPayload("RISPOSTA A: " + segment.payload().getDisplayString());
        Ipv4Address destIp = sourceIp;
        int destPort = segment.sourcePort();

        sendData(payload, destIp, destPort);
    }

    @Override
    protected boolean internalTick()
    {
        return false;
    }
}
