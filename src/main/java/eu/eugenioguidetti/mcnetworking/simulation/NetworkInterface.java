package eu.eugenioguidetti.mcnetworking.simulation;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 26/05/2026
 */

import eu.eugenioguidetti.mcnetworking.Utils;
import eu.eugenioguidetti.mcnetworking.item.CableType;
import eu.eugenioguidetti.mcnetworking.item.ConnectorType;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.protocol.EthernetFrame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

/**
 *
 * @author Eugenio Guidetti
 */
public class NetworkInterface
{
    private record InflightPacket(EthernetFrame frame, int ticksRemaining)
    {
    }

    // Coda di Trasmissione (Buffer)
    private final Queue<InflightPacket> txQueue = new LinkedList<>();


    private MacAddress macAddress = MacAddress.ALL_ZEROS;
    private final BlockPos pos;
    private final Direction direction;
    private ConnectorType connectorType = ConnectorType.RJ45;

    @Nullable
    private BlockPos connectedTargetPos = null;
    @Nullable
    private Direction connectedTargetFace = null;
    @Nullable
    private CableType connectedCableType = null;


    /**
     * Crea un'interfaccia con un indirizzo MAC casuale (Locally Administered, Unicast)
     */

    public NetworkInterface(BlockPos pos, Direction direction, ConnectorType connectorType)
    {
        this.macAddress = MacAddress.generateRandomMac();

        // ! IMPORTANTE ! usare .immutable() senno si sminchia tutto
        this.pos = pos.immutable();
        this.direction = direction;
        this.connectorType = connectorType;
    }

    public NetworkInterface(MacAddress macAddress, BlockPos pos, Direction direction, ConnectorType connectorType)
    {
        this.macAddress = macAddress;

        // ! IMPORTANTE ! usare .immutable() senno si sminchia tutto
        this.pos = pos.immutable();
        this.direction = direction;
        this.connectorType = connectorType;
    }


    // Salvataggio/caricamento dati interfaccia in NBT

    public void save(@NonNull ValueOutput output)
    {
        output.putString("MacAddress", this.getMacAddress().toString());
        output.putString("ConnectorType", this.connectorType.name());

        boolean connected = isConnected();
        output.putBoolean("IsConnected", connected);

        if (connected)
        {
            output.putInt("TargetX", this.connectedTargetPos.getX());
            output.putInt("TargetY", this.connectedTargetPos.getY());
            output.putInt("TargetZ", this.connectedTargetPos.getZ());
            output.putString("TargetFace", this.connectedTargetFace.getName());
            output.putString("CableType", this.connectedCableType.name());
        }
    }

    public void load(@NonNull ValueInput input, Direction face)
    {
        String macStr = input.getString("MacAddress").orElse(null);

        if (macStr == null || macStr.isEmpty())
        {
            this.macAddress = MacAddress.generateRandomMac();
        }
        else
        {
            this.macAddress = new MacAddress(macStr);
        }

        this.connectorType = ConnectorType.fromName(input.getString("ConnectorType").orElse(ConnectorType.RJ45.name()));

        boolean connected = input.getBooleanOr("IsConnected", false);

        if (connected)
        {
            try
            {
                int tx = input.getInt("TargetX").orElseThrow();
                int ty = input.getInt("TargetY").orElseThrow();
                int tz = input.getInt("TargetZ").orElseThrow();
                this.connectedTargetPos = new BlockPos(tx, ty, tz);

                // Uso toLowerCase per sicurezza: se il nome della direzione è maiuscolo non funziona
                this.connectedTargetFace = Direction.byName(input.getString("TargetFace").orElseThrow().toLowerCase());
                this.connectedCableType = CableType.fromName(input.getString("CableType").orElseThrow());
            }
            catch (Exception e)
            {
                this.disconnect();
            }
        }
        else
        {
            this.disconnect();
        }
    }

    /**
     * Chiamato dall'Host/Router quando vuole inviare un pacchetto.
     */
    public void sendPacket(EthernetFrame frame)
    {
        if (!isConnected())
        {
            return;
        }

        txQueue.offer(new InflightPacket(frame, connectedCableType.ticksDelay()));
    }


    public void tick(Level level)
    {
        if (!isConnected() || txQueue.isEmpty())
        {
            return;
        }

        // Prendo il primo pacchetto dalla coda di uscita e diminuisco i tick di ritardo con cui verrà inviato
        InflightPacket inflight = txQueue.poll();
        int ticksLeft = inflight.ticksRemaining() - 1;

        if (ticksLeft > 0)
        {
            // Se c'è ancora da aspettare rimetto il pacchetto in coda
            txQueue.offer(new InflightPacket(inflight.frame(), ticksLeft));
            return;
        }

        // Ritardo esaurito
        BlockEntity target = level.getBlockEntity(this.connectedTargetPos);
        if (target instanceof NetworkReceiver receiver)
        {
            // Il pacchetto entra nel blocco alle coordinate dell'interfaccia di destinazione, specifico da quale faccia arriva

            // Questo codice gira solo sul server

            ServerLevel serverLevel = (ServerLevel) level;
            int color = ARGB.color(255, 255, 0); // Giallo
            Vec3 pos = Utils.getInterfaceCenterPoint(this.getPos(), this.getDirection());

            serverLevel.sendParticles(new DustParticleOptions(color, 1.5f), // color, scale
                                      pos.x, pos.y, pos.z, 1,  // count
                                      0, // delta X
                                      0, // delta Y
                                      0, // delta Z
                                      0  // speed
            );

            receiver.receiveFrame(inflight.frame(), this.connectedTargetFace);
        }
    }


    public void connect(MacAddress connectedMacAddress, BlockPos targetPos, Direction targetFace, CableType cableType)
    {
        this.connectedTargetPos = targetPos;
        this.connectedTargetFace = targetFace;
        this.connectedCableType = cableType;
    }

    public void disconnect()
    {
        this.connectedTargetPos = null;
        this.connectedTargetFace = null;
        this.connectedCableType = null;
    }


    public boolean isConnected()
    {
        boolean disconnected = connectedTargetPos == null || connectedTargetFace == null || connectedCableType == null;

        if (disconnected)
        {
            connectedTargetPos = null;
            connectedTargetFace = null;
            connectedCableType = null;
        }

        return !disconnected;
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public Direction getDirection()
    {
        return direction;
    }

    @Nullable
    public BlockPos getConnectedTargetPos()
    {
        return connectedTargetPos;
    }

    @Nullable
    public Direction getConnectedTargetFace()
    {
        return connectedTargetFace;
    }

    @Nullable
    public CableType getConnectedCableType()
    {
        return connectedCableType;
    }

    public ConnectorType getConnectorType()
    {
        return connectorType;
    }

    public MacAddress getMacAddress()
    {
        return macAddress;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        NetworkInterface that = (NetworkInterface) o;
        return Objects.equals(getMacAddress(), that.getMacAddress()) && Objects.equals(getPos(),
                                                                                       that.getPos()) && getDirection() == that.getDirection();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getMacAddress(), getPos(), getDirection());
    }

    @Override
    public String toString()
    {
        String s = "NetworkInterface{" + "macAddress=" + macAddress + ", pos=" + pos.toShortString() + ", dir=" + direction + ", connectorType=" + connectorType;

        if (isConnected())
        {
            s += ", connectedTargetPos=" + connectedTargetPos.toShortString() + ", connectedTargetFace=" + connectedTargetFace + ", connectedCableType=" + connectedCableType;
        }

        s += '}';

        return s;
    }
}