package eu.eugenioguidetti.mcnetworking.block.entity;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 03/06/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.client.rendering.CablesRenderPipeline;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkReceiver;
import eu.eugenioguidetti.mcnetworking.simulation.logic.NetworkStack;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.Job;
import eu.eugenioguidetti.mcnetworking.simulation.models.Ipv4CidrAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.MacAddress;
import eu.eugenioguidetti.mcnetworking.simulation.models.cables.CableType;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalCache;
import eu.eugenioguidetti.mcnetworking.terminal.gui.ClientCommandHistoryCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static eu.eugenioguidetti.mcnetworking.GlobalConstants.LOOPBACK_NAME;
import static eu.eugenioguidetti.mcnetworking.GlobalConstants.MAX_JOBS;

/**
 *
 * @author Eugenio Guidetti
 */
public abstract class NetworkingBlockEntity extends BlockEntity implements NetworkReceiver
{
    protected final NetworkStack stack;

    protected String hostname;

    private final Map<String, NetworkInterface> nics = new HashMap<>();
    private final Map<Direction, String> physicalPortsNames = new EnumMap<>(Direction.class);

    protected final Map<Integer, Job> activeJobs = new HashMap<>();
    protected int nextJobId = 1;
    protected int foregroundJobId = -1;

    public NetworkingBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState)
    {
        super(type, worldPosition, blockState);

        this.stack = new NetworkStack(this);

        NetworkInterface loopbackInterface = new NetworkInterface(LOOPBACK_NAME, getBlockPos(), null, null);
        loopbackInterface.setIpAddress(Ipv4CidrAddress.LOOPBACK);
        putInterface(loopbackInterface);
    }


    public String getHostname()
    {
        return hostname;
    }

    public void setHostname(String hostname)
    {
        this.hostname = hostname;
        this.sync();
    }


    @Override
    public NetworkStack getStack()
    {
        return stack;
    }

    @Override
    public void putInterface(@NotNull NetworkInterface networkInterface)
    {
        String name = networkInterface.getName();
        Direction direction = networkInterface.getDirection();

        nics.put(name, networkInterface);

        if (direction != null)
        {
            physicalPortsNames.put(direction, name);
        }
    }

    @Override
    public NetworkInterface getInterface(@NonNull String nicName)
    {
        return nics.get(nicName);
    }

    @Override
    public NetworkInterface getInterface(Direction face)
    {
        return nics.get(getInterfaceName(face));
    }

    @Override
    public NetworkInterface getInterface(MacAddress macAddress)
    {
        for (NetworkInterface nic : nics.values())
        {
            if (nic.getMacAddress().equals(macAddress))
            {
                return nic;
            }
        }

        return null;
    }

    @Override
    public String getInterfaceName(Direction face)
    {
        if (face == null || !physicalPortsNames.containsKey(face))
        {
            return null;
        }

        return physicalPortsNames.get(face);
    }

    @Override
    public Map<String, NetworkInterface> getNics()
    {
        return nics;
    }

    @Override
    public void disconnectPhysical(Direction face)
    {
        if (this.level == null || this.level.isClientSide() || face == null)
        {
            return;
        }

        disconnectNic(nics.get(getInterfaceName(face)));
    }

    @Override
    public void disconnectAllPhysical()
    {
        if (this.level == null || this.level.isClientSide())
        {
            return;
        }

        for (String nicName : this.physicalPortsNames.values())
        {
            disconnectNic(nics.get(nicName));
        }
    }

    private void disconnectNic(NetworkInterface nic)
    {
        if (nic == null || !nic.isConnected())
        {
            return;
        }

        BlockPos targetPos = nic.getPhysicalConnectionData().connectedTargetPos();
        String targetName = nic.getPhysicalConnectionData().connectedTargetName();

        CableType cableType = nic.getPhysicalConnectionData().connectedCableType();

        if (this.getLevel() != null)
        {
            ItemStack dropStack = new ItemStack(cableType.getAsItem());

            Containers.dropItemStack(this.getLevel(),
                                     this.worldPosition.getX() + 0.5,
                                     this.worldPosition.getY() + 0.5,
                                     this.worldPosition.getZ() + 0.5,
                                     dropStack);
        }


        BlockEntity targetEntity = this.getLevel().getBlockEntity(targetPos);
        if ((targetEntity instanceof NetworkReceiver receiver))
        {
            NetworkInterface remoteNic = receiver.getInterface(targetName);
            if (remoteNic != null)
            {
                remoteNic.disconnect();
            }

            receiver.sync();
        }

        nic.disconnect();
        sync();
    }


    public int allocateJobId()
    {
        int assignedId = nextJobId;
        nextJobId++;

        if (nextJobId > MAX_JOBS)
        {
            nextJobId = 1;
        }

        return assignedId;
    }

    public void startJob(@NonNull Job job)
    {
        int jobId = allocateJobId();
        this.activeJobs.put(jobId, job);
        foregroundJobId = jobId;

        job.setId(jobId);
    }

    public Optional<Job> getJob(int jobId)
    {
        if (jobId < 1 || jobId > MAX_JOBS)
        {
            MCNetworking.LOGGER.error("Ricevuto jobId invalido: {}", jobId);
        }

        return Optional.ofNullable(activeJobs.get(jobId));
    }

    public Optional<Job> getLastJob()
    {
        int lastJobId = activeJobs.keySet().stream().max(Integer::compareTo).orElse(-1);
        return Optional.ofNullable(activeJobs.get(lastJobId));
    }

    public Optional<Job> getForegroundJob()
    {
        return Optional.ofNullable(activeJobs.get(foregroundJobId));
    }

    // --- Metodi minecraft ---


    public static <E extends NetworkingBlockEntity> void serverTick(@NotNull Level level, BlockPos pos, BlockState state, E entity)
    {
        if (level.isClientSide())
        {
            return;
        }

        entity.tickServer(level);
    }

    public static <E extends NetworkingBlockEntity> void clientTick(@NotNull Level level, BlockPos pos, BlockState state, E entity)
    {
        if (!level.isClientSide())
        {
            return;
        }

        for (NetworkInterface nic : entity.getNics().values())
        {
            if (nic.isLoopback())
            {
                continue;
            }

            if (!nic.isConnected() || entity.isRemoved() || entity.getLevel() == null)
            {
                CablesRenderPipeline.removeCable(GlobalPos.of(level.dimension(), nic.getPos()), nic.getDirection());

                continue;
            }

            // Evita di mostrare "connessioni fantasma"
            if (!(entity
                    .getLevel()
                    .getBlockEntity(nic.getPhysicalConnectionData().connectedTargetPos()) instanceof NetworkReceiver receiver))
            {
                nic.disconnect();

                continue;
            }

            if (!receiver.getInterface(nic.getPhysicalConnectionData().connectedTargetName()).isConnected())
            {
                nic.disconnect();

                continue;
            }

            CablesRenderPipeline.addCable(GlobalPos.of(entity.getLevel().dimension(), nic.getPos()),
                                          nic.getDirection(),
                                          GlobalPos.of(entity.getLevel().dimension(), nic.getPhysicalConnectionData().connectedTargetPos()),
                                          nic.getPhysicalConnectionData().connectedTargetFace(),
                                          nic.getPhysicalConnectionData().connectedCableType());
        }
    }


    public void tickServer(Level level)
    {
        for (NetworkInterface nic : nics.values())
        {
            nic.tick(level);
        }

        // Rimuove i job completati (quando tick() restituisce true)
        if (activeJobs.values().removeIf(Job::tick))
        {
            // Almeno un job è terminato
            ConsoleSession session = TerminalCache.getOrCreateSession(this).session();
            session.updateOutput();
        }
    }

    // Sincronizza i dati con i client
    @Override
    public void sync()
    {
        if (this.level == null || this.level.isClientSide())
        {
            return;
        }

        // Segna il chunk come "sporco" così Minecraft lo salverà su disco
        this.setChanged();
        // Invia il pacchetto di aggiornamento
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    // Salvataggio/caricamento dati blocco in NBT
    @Override
    protected void saveAdditional(@NonNull ValueOutput output)
    {
        super.saveAdditional(output);

        output.putString("Hostname", this.hostname);

        ValueOutput nicsOutput = output.child("NetworkInterfaces");

        for (Map.Entry<String, NetworkInterface> entry : this.nics.entrySet())
        {
            String name = entry.getKey();
            if (name.equals(LOOPBACK_NAME))
            {
                continue;
            }

            NetworkInterface nic = entry.getValue();

            ValueOutput nicOutput = nicsOutput.child(name);
            nic.save(nicOutput);
        }
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input)
    {
        super.loadAdditional(input);

        input.getString("Hostname").ifPresent((hostname) -> this.hostname = hostname);

        ValueInput nicsInput = input.child("NetworkInterfaces").orElse(null);

        if (nicsInput == null)
        {
            disconnectAllPhysical();

            return;
        }

        for (Map.Entry<String, NetworkInterface> entry : this.nics.entrySet())
        {
            String name = entry.getKey();
            NetworkInterface nic = entry.getValue();

            ValueInput specificNicInput = nicsInput.child(name).orElse(null);

            if (specificNicInput != null)
            {
                nic.load(specificNicInput);

                physicalPortsNames.put(nic.getDirection(), name);
            }
            else
            {
                nic.disconnect();
            }
        }
    }


    // --- Aggiornamento server->client dello stato delle block entity ---

    // Definisce il pacchetto che verrà spedito al client
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // Prepara i dati da inserire nel pacchetto
    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries)
    {
        return this.saveCustomOnly(registries);
    }


    @Override
    public void setRemoved()
    {
        if (this.level != null)
        {
            if (this.level.isClientSide())
            {
                CablesRenderPipeline.removeCablesFromBlock(GlobalPos.of(this.level.dimension(), this.getBlockPos()));
            }
            else
            {
                activeJobs.clear();
            }
        }

        super.setRemoved();
    }

    // Blocco distrutto
    @Override
    public void preRemoveSideEffects(@NonNull BlockPos pos, @NonNull BlockState state)
    {
        activeJobs.clear();

        if (this.level == null)
        {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof NetworkingBlockEntity netEntity)
        {
            netEntity.disconnectAllPhysical();
            TerminalCache.removeBlock(level, pos);

            if (level.isClientSide())
            {
                CablesRenderPipeline.removeCablesFromBlock(GlobalPos.of(this.level.dimension(), pos));
            }

            ClientCommandHistoryCache.clearCache(GlobalPos.of(this.level.dimension(), pos));
        }

        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + " Hostname: " + hostname + " at: " + getBlockPos().toShortString();
    }
}
