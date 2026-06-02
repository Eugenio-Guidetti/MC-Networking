package eu.eugenioguidetti.mcnetworking.simulation;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 26/05/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.item.CableType;
import eu.eugenioguidetti.mcnetworking.item.ConnectorType;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    private record InflightPacket(SimPacket packet, int ticksRemaining)
    {
    }

    // Coda di Trasmissione (Buffer)
    private final Queue<InflightPacket> txQueue = new LinkedList<>();


    private MacAddress macAddress = MacAddress.ALL_ZEROS;
    private final BlockPos pos;
    private Direction direction = Direction.NORTH;
    private ConnectorType connectorType = ConnectorType.RJ45;

    @Nullable
    private MacAddress connectedMacAddress = null;
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
        if (direction == null)
        {
            throw new IllegalArgumentException("direction is null");
        }

        this.macAddress = MacAddress.generateRandomMac();
        this.pos = pos;
        this.direction = direction;
        this.connectorType = connectorType;
    }

    public NetworkInterface(String macString, BlockPos pos, Direction direction, ConnectorType connectorType)
    {
        this.macAddress = new MacAddress(macString);
        this.pos = pos;
        this.direction = direction;
        this.connectorType = connectorType;
    }

    public NetworkInterface(byte[] macBytes, BlockPos pos, Direction direction, ConnectorType connectorType)
    {
        this.macAddress = new MacAddress(macBytes);
        this.pos = pos;
        this.direction = direction;
        this.connectorType = connectorType;
    }

    public NetworkInterface(MacAddress macAddress, BlockPos pos, Direction direction, ConnectorType connectorType)
    {
        this.macAddress = macAddress;
        this.pos = pos;
        this.direction = direction;
        this.connectorType = connectorType;
    }


    // Salvataggio dati interfaccia in NBT
    public void save(@NonNull ValueOutput output)
    {
        output.putString("MacAddress", this.getMacAddress().toString());
        output.putString("ConnectorType", this.connectorType.name());

        boolean connected = isConnected();
        output.putBoolean("IsConnected", connected);

        if (connected)
        {
            output.putString("ConnectedMacAddress", this.getConnectedMacAddress().toString());

            output.putInt("TargetX", this.connectedTargetPos.getX());
            output.putInt("TargetY", this.connectedTargetPos.getY());
            output.putInt("TargetZ", this.connectedTargetPos.getZ());

            output.putString("TargetFace", this.connectedTargetFace.name());
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

        this.direction = face;
        this.connectorType = ConnectorType.fromName(input.getString("ConnectorType").orElse(ConnectorType.RJ45.name()));

        boolean connected = input.getBooleanOr("IsConnected", false);

        if (connected)
        {
            try
            {
                this.connectedMacAddress = new MacAddress(input.getString("ConnectedMacAddress").orElseThrow());

                int x = input.getInt("TargetX").orElseThrow();
                int y = input.getInt("TargetY").orElseThrow();
                int z = input.getInt("TargetZ").orElseThrow();
                this.connectedTargetPos = new BlockPos(x, y, z);

                this.connectedTargetFace = Direction.valueOf(input.getString("TargetFace").orElseThrow());
                this.connectedCableType = CableType.fromName(input.getString("CableType").orElseThrow());
            }
            catch (Exception e)
            {
                // FONDAMENTALE: Stampiamo l'errore in console, così non sarà mai più "silenzioso"!
                MCNetworking.LOGGER.error("Errore durante il caricamento della NIC!", e);
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
    public void sendPacket(SimPacket packet)
    {
        if (!isConnected())
        {
            return;
        }

        // Mettiamo il pacchetto nella coda d'uscita (Transmit Buffer) applicando il ritardo del cavo
        txQueue.offer(new InflightPacket(packet, connectedCableType.ticksDelay()));
    }


    public void tick(net.minecraft.world.level.Level level)
    {
        if (txQueue.isEmpty() || !isConnected())
        {
            return;
        }

        // Prendo il primo pacchetto dalla coda di uscita e diminuisco i tick di ritardo con cui verrà inviato
        InflightPacket inflight = txQueue.poll();
        int ticksLeft = inflight.ticksRemaining() - 1;

        if (ticksLeft > 0)
        {
            // Se c'è ancora da aspettare rimetto il pacchetto in coda
            txQueue.offer(new InflightPacket(inflight.packet(), ticksLeft));
            return;
        }

        // Ritardo esaurito
        BlockEntity target = level.getBlockEntity(this.connectedTargetPos);
        if (target instanceof NetworkReceiver receiver)
        {
            // Il pacchetto entra nel blocco alle coordinate dell'interfaccia di destinazione, specifico da quale faccia arriva
            receiver.receivePacket(inflight.packet(), this.connectedTargetFace);
        }
    }


    public void connect(MacAddress connectedMacAddress, BlockPos targetPos, Direction targetFace, CableType cableType)
    {
        this.connectedMacAddress = connectedMacAddress;
        this.connectedTargetPos = targetPos;
        this.connectedTargetFace = targetFace;
        this.connectedCableType = cableType;
    }

    public void disconnect()
    {
        this.connectedMacAddress = null;
        this.connectedTargetPos = null;
        this.connectedTargetFace = null;
        this.connectedCableType = null;
    }


    public boolean isConnected()
    {
        boolean disconnected = connectedMacAddress == null || connectedTargetPos == null || connectedTargetFace == null || connectedCableType == null;

        if (disconnected)
        {
            connectedMacAddress = null;
            connectedTargetPos = null;
            connectedTargetFace = null;
            connectedCableType = null;
        }

        return !disconnected;
    }

    public MacAddress getConnectedMacAddress()
    {
        return connectedMacAddress;
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public Direction getDirection()
    {
        if (direction == null)
        {
            //throw new IllegalStateException("direction is null");

            direction = Direction.NORTH;
        }

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
        return Objects.equals(getMacAddress(), that.getMacAddress()) && Objects.equals(getConnectorType(), that.getConnectorType());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getMacAddress(), getConnectorType());
    }

    @Override
    public String toString()
    {
        return "NetworkInterface{" + "macAddress=" + macAddress + ", pos=" + pos.toShortString() + ", direction=" + direction + ", connectorType=" + connectorType + ", connectedMacAddress=" + connectedMacAddress + ", connectedTargetPos=" + connectedTargetPos.toShortString() + ", connectedTargetFace=" + connectedTargetFace + ", connectedCableType=" + connectedCableType + '}';
    }
}