package eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.l4;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 17/08/2026
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
public class TestClient extends L4Job
{
    private boolean ricevuto = false;
    private final int destPort;

    public TestClient(HostBlockEntity netEntity, TransportProtocol protocol, int destPort)
    {
        super(netEntity, protocol);

        this.destPort = destPort;
    }

    @Override
    protected void onL4Start()
    {
        super.onL4Start();

        session.updateOutput();

        ApplicationPayload payload = new ApplicationPayload("MESSAGGIO");
        Ipv4Address destIp = new Ipv4Address("192.168.1.15");

        sendData(payload, destIp, destPort);
    }

    @Override
    public void onSegmentReceived(@NonNull TransportSegment segment, @NonNull Ipv4Address sourceIp)
    {
        session.sendOutput(segment + "\n");

        ricevuto = true;
    }

    @Override
    protected boolean internalTick()
    {
        return ricevuto;
    }
}
