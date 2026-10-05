package eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.l4;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 03/10/2026
 */

import eu.eugenioguidetti.mcnetworking.GlobalConstants;
import eu.eugenioguidetti.mcnetworking.block.entity.HostBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.logic.endDevices.EndDeviceL4Engine;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.Job;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.NetworkPayload;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.TransportProtocol;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.TransportSegment;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public abstract class L4Job extends Job
{
    protected final HostBlockEntity l4netEntity;
    private final EndDeviceL4Engine l4Engine;
    private final TransportProtocol protocol;
    private final boolean isDynamicPort;
    private int localPort;

    /**
     * Costruttore per CLIENT (allocazione dinamica porta)
     */
    protected L4Job(@NonNull HostBlockEntity l4netEntity, TransportProtocol protocol)
    {
        super(l4netEntity);

        this.l4netEntity = l4netEntity;
        this.l4Engine = (EndDeviceL4Engine) l4netEntity.getStack().getL4Engine();
        this.protocol = protocol;
        this.isDynamicPort = true;
        this.localPort = -1; // Non ancora assegnata
    }

    /**
     * Costruttore per SERVER / DEMONI (bind porta specifica)
     */
    protected L4Job(@NonNull HostBlockEntity l4netEntity, TransportProtocol protocol, int port)
    {
        super(l4netEntity);
        TransportSegment.validatePort(port);

        this.l4netEntity = l4netEntity;
        this.l4Engine = (EndDeviceL4Engine) l4netEntity.getStack().getL4Engine();
        this.protocol = protocol;
        this.isDynamicPort = false;
        this.localPort = port;
    }

    @Override
    public final void onStart()
    {
        if (isDynamicPort)
        {
            this.localPort = l4Engine.allocateDynamicPort(protocol, this);
            if (this.localPort < GlobalConstants.WELl_KNOWN_PORTS_FIRST) // allocateDynamicPort restituisce -1 solo se fallisce (porte esaurite)
            {
                session.sendOutput("Non è stato possibile allocare una porta dinamica\n");
                terminate();
                return;
            }
        }
        else
        {
            if (!l4Engine.bindPort(this.localPort, protocol, this))
            {
                session.sendOutput(String.format("Porta %s già in uso\n", this.localPort));
                terminate();
                return;
            }
        }

        onL4Start();
    }

    protected void onL4Start()
    {
    }

    protected void sendData(NetworkPayload payload, Ipv4Address destIp, int destPort)
    {
        l4Engine.sendPayload(payload, destIp, this.localPort, destPort, this.protocol);
    }

    public abstract void onSegmentReceived(@NonNull TransportSegment segment, @NonNull Ipv4Address sourceIp);

    @Override
    protected final void onTerminate()
    {
        try
        {
            onL4Terminate();
        }
        finally
        {
            l4Engine.unbindJobPorts(this);
            super.onTerminate();
        }
    }

    protected void onL4Terminate()
    {

    }

    public int getLocalPort()
    {
        return localPort;
    }
}
