package eu.eugenioguidetti.mcnetworking.simulation;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 26/05/2026
 */

import eu.eugenioguidetti.mcnetworking.GlobalConstants;
import eu.eugenioguidetti.mcnetworking.Utils;
import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4CidrAddress;
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
import org.jspecify.annotations.NonNull;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

import static eu.eugenioguidetti.mcnetworking.GlobalConstants.LOOPBACK_NAME;

/**
 *
 * @author Eugenio Guidetti
 */


public class NetworkInterface
{
    /**
     *
     * @param connectedTargetName il nome dell'interfaccia connessa (es. 'eth0')
     */
    public record PhysicalConnectionData(@NonNull BlockPos connectedTargetPos, @NonNull String connectedTargetName,
                                         @NonNull Direction connectedTargetFace, @NonNull CableType connectedCableType, double cableLength)
    {
        public PhysicalConnectionData(BlockPos connectedTargetPos,
                                      String connectedTargetName,
                                      Direction connectedTargetFace,
                                      CableType connectedCableType,
                                      double cableLength)
        {
            if (connectedTargetPos == null || connectedTargetName == null || connectedTargetFace == null || connectedCableType == null)
            {
                throw new IllegalArgumentException("Parametro null");
            }

            if (cableLength <= 0)
            {
                throw new IllegalArgumentException("CableLength negativo");
            }
            if (cableLength * cableLength > GlobalConstants.CABLES_MAX_LENGTH_SQUARED)
            {
                throw new IllegalArgumentException("Dispositivi troppo lontani");
            }

            this.connectedTargetPos = connectedTargetPos;
            this.connectedTargetName = connectedTargetName;
            this.connectedTargetFace = connectedTargetFace;
            this.connectedCableType = connectedCableType;
            this.cableLength = cableLength;
        }
    }


    // Coda di Trasmissione (Buffer)
    private final Queue<EthernetFrame> txQueue = new LinkedList<>();

    private final String name;
    private final BlockPos pos;
    private final Direction direction;

    private ConnectorType connectorType = ConnectorType.RJ45;
    private final float txSpeed = 2500f; // [Byte / s]

    private EthernetFrame currentTxFrame = null;
    private int currentTxTicksRemaining = 0;

    private MacAddress macAddress = null;
    private Ipv4CidrAddress ipAddress = null;

    private PhysicalConnectionData physicalConnectionData = null;

    /**
     * Crea un'interfaccia con un indirizzo MAC casuale (Locally Administered, Unicast)
     */
    public NetworkInterface(@NotNull String name, @NotNull BlockPos pos, Direction direction, ConnectorType connectorType)
    {
        this.macAddress = MacAddress.generateRandomMac();

        this.name = name.toLowerCase();
        this.pos = pos.immutable(); // ! IMPORTANTE ! Usare .immutable() senno si sminchia tutto
        this.direction = direction;
        this.connectorType = connectorType;

        this.ipAddress = Ipv4CidrAddress.ALL_ZEROS;
    }

    public NetworkInterface(MacAddress macAddress,
                            @NotNull String name,
                            @NotNull BlockPos pos,
                            Direction direction,
                            ConnectorType connectorType)
    {
        if (macAddress == null || macAddress.equals(MacAddress.ALL_ZEROS))
        {
            this.macAddress = MacAddress.generateRandomMac();
        }
        else
        {
            this.macAddress = macAddress;
        }

        this.name = name.toLowerCase();
        this.pos = pos.immutable(); // ! IMPORTANTE ! Usare .immutable() senno si sminchia tutto
        this.direction = direction;
        this.connectorType = connectorType;

        this.ipAddress = Ipv4CidrAddress.ALL_ZEROS;
    }

    /**
     * Chiamato dall'Host/Router quando vuole inviare un pacchetto.
     */
    public void sendFrame(@NotNull EthernetFrame frame)
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
            currentTxTicksRemaining = (int) (physicalConnectionData.cableLength() / physicalConnectionData
                    .connectedCableType()
                    .cableSpeed() + (currentTxFrame.getSizeInBytes() * 20 / this.txSpeed));
        }

        // Eseguo ritardo
        if (currentTxTicksRemaining > 0)
        {
            currentTxTicksRemaining--;

            if (currentTxTicksRemaining % 5 == 0)
            {
                showParticles((ServerLevel) level);
            }

            return;
        }

        // Ritardo esaurito
        BlockEntity target = level.getBlockEntity(this.physicalConnectionData.connectedTargetPos());
        if (target instanceof NetworkingBlockEntity netEntity)
        {
            // Il pacchetto entra nel blocco alle coordinate dell'interfaccia di destinazione, specifico da quale faccia arriva
            showParticles((ServerLevel) level);
            netEntity.getStack().receiveFrame(currentTxFrame, this.physicalConnectionData.connectedTargetName());
        }

        currentTxFrame = null;
    }

    private void showParticles(@NonNull ServerLevel serverLevel)
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


    // Salvataggio/caricamento dati interfaccia in NBT

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
            output.putInt("TargetX", physicalConnectionData.connectedTargetPos().getX());
            output.putInt("TargetY", physicalConnectionData.connectedTargetPos().getY());
            output.putInt("TargetZ", physicalConnectionData.connectedTargetPos().getZ());
            output.putString("TargetName", physicalConnectionData.connectedTargetName());
            output.putString("TargetFace", physicalConnectionData.connectedTargetFace().getName());
            output.putString("CableType", physicalConnectionData.connectedCableType().name());
            output.putDouble("CableLength", physicalConnectionData.cableLength());
        }

        if (ipAddress != null && !ipAddress.equals(Ipv4CidrAddress.ALL_ZEROS))
        {
            output.putString("IpAddress", this.ipAddress.toString());
        }
    }

    public void load(@NonNull ValueInput input)
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
                BlockPos connectedTargetPos = new BlockPos(tx, ty, tz);

                String connectedTargetName = input.getString("TargetName").orElseThrow();

                // Se il nome della direzione è maiuscolo non funziona
                String connectedTargetFaceName = input.getString("TargetFace").orElseThrow();
                Direction connectedTargetFace = Direction.byName(connectedTargetFaceName.toLowerCase());

                CableType connectedCableType = CableType.fromName(input.getString("CableType").orElseThrow());
                double cableLength = input.getDoubleOr("CableLength", -1);

                connect(new PhysicalConnectionData(connectedTargetPos,
                                                   connectedTargetName,
                                                   connectedTargetFace,
                                                   connectedCableType,
                                                   cableLength));
            }
            catch (Exception e)
            {
                disconnect();
            }
        }
        else
        {
            this.disconnect();
        }

        this.ipAddress = input.getString("IpAddress").map(Ipv4CidrAddress::new).orElse(Ipv4CidrAddress.ALL_ZEROS);
    }

    public boolean isLoopback()
    {
        return this.name.equals(LOOPBACK_NAME);
    }

    public void connect(PhysicalConnectionData connectionData)
    {
        if (isLoopback())
        {
            disconnect();

            throw new IllegalStateException("Non puoi connettere fisicamente un'interfaccia di loopback");
        }

        this.physicalConnectionData = connectionData;
        //BlockPos targetPos, String targetName, Direction connectedTargetFace, CableType cableType, int cableLength
        //new PhysicalConnectionData(targetPos, targetName, connectedTargetFace, cableType, cableLength);
    }

    public void disconnect()
    {
        this.physicalConnectionData = null;
    }

    public boolean isConnected()
    {
        return physicalConnectionData != null;
    }


    public String getName()
    {
        return name;
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

    public PhysicalConnectionData getPhysicalConnectionData()
    {
        return physicalConnectionData;
    }


    public Ipv4CidrAddress getIpAddress()
    {
        return ipAddress;
    }

    public void setIpAddress(@NotNull Ipv4CidrAddress ipAddress)
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
}