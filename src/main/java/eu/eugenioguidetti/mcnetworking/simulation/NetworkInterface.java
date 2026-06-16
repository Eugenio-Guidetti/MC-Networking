package eu.eugenioguidetti.mcnetworking.simulation;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 26/05/2026
 */

import eu.eugenioguidetti.mcnetworking.Utils;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4Address;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.cables.CableType;
import eu.eugenioguidetti.mcnetworking.simulation.models.cables.ConnectorType;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
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
import org.jetbrains.annotations.NotNull;
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
    // Coda di Trasmissione (Buffer)
    private final Queue<EthernetFrame> txQueue = new LinkedList<>();

    private final BlockPos pos;
    private final Direction direction;
    private ConnectorType connectorType = ConnectorType.RJ45;
    private float txSpeed = 0.5f;

    private EthernetFrame currentTxFrame = null;
    private int currentTxTicksRemaining = 0;

    private MacAddress macAddress = MacAddress.ALL_ZEROS;
    private Ipv4Address ipAddress = Ipv4Address.ALL_ZEROS;

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

        this.ipAddress = Ipv4Address.ALL_ZEROS;
    }


    public NetworkInterface(MacAddress macAddress, BlockPos pos, Direction direction, ConnectorType connectorType)
    {
        this.macAddress = macAddress;

        // ! IMPORTANTE ! usare .immutable() senno si sminchia tutto
        this.pos = pos.immutable();
        this.direction = direction;
        this.connectorType = connectorType;

        this.ipAddress = Ipv4Address.ALL_ZEROS;
    }

    /**
     * Chiamato dall'Host/Router quando vuole inviare un pacchetto.
     */
    public void sendFrame(EthernetFrame frame)
    {
        if (!isConnected())
        {
            return;
        }


        txQueue.offer(frame);
    }

    public void tick(Level level)
    {
        if (!isConnected() || (txQueue.isEmpty() && currentTxFrame == null))
        {
            return;
        }

        // Prendo il frame da trasmettere
        if (currentTxFrame == null)
        {
            currentTxFrame = txQueue.poll();

            // Calcolo ritardo
            currentTxTicksRemaining = connectedCableType.ticksDelay() + (int) (currentTxFrame.getSizeInBytes() * this.txSpeed);
        }

        // Eseguo ritardo
        if (currentTxTicksRemaining > 0)
        {
            currentTxTicksRemaining--;

            if (currentTxTicksRemaining % 7 == 0)
            {
                showParticles((ServerLevel) level);
            }

            return;
        }

        // Ritardo esaurito
        BlockEntity target = level.getBlockEntity(this.connectedTargetPos);
        if (target instanceof NetworkReceiver receiver)
        {


            // Il pacchetto entra nel blocco alle coordinate dell'interfaccia di destinazione, specifico da quale faccia arriva
            receiver.receiveFrame(currentTxFrame, this.connectedTargetFace);
        }

        currentTxFrame = null;
    }

    private void showParticles(ServerLevel serverLevel)
    {
        int color = ARGB.color(255, 255, 0); // Giallo
        Vec3 pos = Utils.getInterfaceCenterPoint(this.getPos(), this.getDirection());

        pos = new Vec3(pos.x + ((pos.x - (getPos().getX() + .5f))) * .6f, pos.y + 0f, pos.z + ((pos.z - (getPos().getZ() + .5f))) * .6f);

        serverLevel.sendParticles(new DustParticleOptions(color, 1.5f), // color, scale
                                  pos.x, pos.y, pos.z, 1,  // count
                                  .1f, // delta X
                                  .1f, // delta Y
                                  .1f, // delta Z
                                  10  // speed
        );
    }


    public void save(@NonNull ValueOutput output)
    {
        if (!this.getMacAddress().equals(MacAddress.ALL_ZEROS))
        {
            output.putString("MacAddress", this.getMacAddress().toString());
        }

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

        if (ipAddress != null && !ipAddress.equals(Ipv4Address.ALL_ZEROS))
        {
            output.putString("IpAddress", this.ipAddress.toString());
        }
    }


    // Salvataggio/caricamento dati interfaccia in NBT

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

        this.ipAddress = input.getString("IpAddress").map(Ipv4Address::new).orElse(Ipv4Address.ALL_ZEROS);
    }


    public void connect(BlockPos targetPos, Direction targetFace, CableType cableType)
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

    public ConnectorType getConnectorType()
    {
        return connectorType;
    }

    public MacAddress getMacAddress()
    {
        return macAddress;
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

    public Ipv4Address getIpAddress()
    {
        return ipAddress;
    }

    public void setIpAddress(@NotNull Ipv4Address ipAddress)
    {
        this.ipAddress = ipAddress;
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