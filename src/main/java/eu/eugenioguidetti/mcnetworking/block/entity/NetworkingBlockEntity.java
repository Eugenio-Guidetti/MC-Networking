package eu.eugenioguidetti.mcnetworking.block.entity;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 03/06/2026
 */

import eu.eugenioguidetti.mcnetworking.client.rendering.CablesRenderPipeline;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkReceiver;
import eu.eugenioguidetti.mcnetworking.simulation.logic.NetworkStack;
import eu.eugenioguidetti.mcnetworking.simulation.models.cables.CableType;
import eu.eugenioguidetti.mcnetworking.simulation.models.protocol.EthernetFrame;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalCache;
import eu.eugenioguidetti.mcnetworking.terminal.gui.CommandHistoryCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public abstract class NetworkingBlockEntity extends BlockEntity implements NetworkReceiver
{
    protected final Map<Direction, NetworkInterface> nics = new EnumMap<>(Direction.class);
    protected String hostname;

    protected NetworkStack stack;

    public NetworkingBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState)
    {
        super(type, worldPosition, blockState);

        this.stack = new NetworkStack(this);
    }

    public static <E extends NetworkingBlockEntity> void serverTick(Level level, BlockPos pos, BlockState state, E entity)
    {
        if (level.isClientSide())
        {
            return;
        }

        for (NetworkInterface nic : entity.nics.values())
        {
            nic.tick(level);
        }
    }

    public static <E extends NetworkingBlockEntity> void clientTick(Level level, BlockPos pos, BlockState state, E entity)
    {
        if (!level.isClientSide())
        {
            return;
        }

        for (NetworkInterface nic : entity.nics.values())
        {
            if (nic.isConnected())
            {
                CablesRenderPipeline.addCable(nic.getPos(),
                                              nic.getDirection(),
                                              nic.getConnectedTargetPos(),
                                              nic.getConnectedTargetFace(),
                                              nic.getConnectedCableType());
            }
            else
            {
                CablesRenderPipeline.removeCable(nic.getPos(), nic.getDirection());
            }
        }
    }

    @Override
    public void receiveFrame(@NotNull EthernetFrame frame, @NotNull Direction from)
    {
        if (this.level == null || this.level.isClientSide() || this.stack == null)
        {
            return;
        }

        this.stack.receiveFrame(frame, from);
    }

    @Override
    public NetworkInterface getInterface(Direction face)
    {
        return nics.get(face);
    }

    @Override
    public Map<Direction, NetworkInterface> getNics()
    {
        return nics;
    }

    @Override
    public void disconnectAll()
    {
        if (this.level == null || this.level.isClientSide())
        {
            return;
        }

        for (NetworkInterface nic : this.nics.values())
        {
            disconnectNic(nic);
        }
    }

    private void disconnectNic(@NotNull NetworkInterface nic)
    {
        if (!nic.isConnected())
        {
            return;
        }

        BlockPos targetPos = nic.getConnectedTargetPos();
        Direction targetFace = nic.getConnectedTargetFace();
        CableType cableType = nic.getConnectedCableType();

        ItemStack dropStack = new ItemStack(cableType.getAsItem());

        Containers.dropItemStack(this.level,
                                 this.worldPosition.getX() + 0.5,
                                 this.worldPosition.getY() + 0.5,
                                 this.worldPosition.getZ() + 0.5,
                                 dropStack);


        BlockEntity targetEntity = this.level.getBlockEntity(targetPos);
        if ((targetEntity instanceof NetworkReceiver receiver))
        {
            NetworkInterface remoteNic = receiver.getInterface(targetFace);
            if (remoteNic != null)
            {
                remoteNic.disconnect();
            }

            receiver.sync();
        }

        nic.disconnect();
        sync();
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


    // --- Salvataggio/caricamento dati blocco in NBT ---

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);

        output.putString("Hostname", this.hostname);

        ValueOutput nicsOutput = output.child("NetworkInterfaces");

        for (Map.Entry<Direction, NetworkInterface> entry : this.nics.entrySet())
        {
            Direction face = entry.getKey();
            NetworkInterface nic = entry.getValue();

            ValueOutput nicOutput = nicsOutput.child(face.getName());
            nic.save(nicOutput);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);

        input.getString("Hostname").ifPresent((hostname) -> this.hostname = hostname);

        ValueInput nicsInput = input.child("NetworkInterfaces").orElse(null);

        if (nicsInput == null)
        {
            for (NetworkInterface nic : this.nics.values())
            {
                nic.disconnect();
            }

            return;
        }

        for (Map.Entry<Direction, NetworkInterface> entry : this.nics.entrySet())
        {
            Direction face = entry.getKey();
            NetworkInterface nic = entry.getValue();

            ValueInput specificNicInput = nicsInput.child(face.getName()).orElse(null);

            if (specificNicInput != null)
            {
                nic.load(specificNicInput, face);
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
    public CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        return this.saveCustomOnly(registries);
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


    // Blocco distrutto
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state)
    {
        if (this.level == null)
        {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof NetworkingBlockEntity netEntity)
        {
            netEntity.disconnectAll();
            TerminalCache.clearBlock(level, pos);

            if (level.isClientSide())
            {
                CablesRenderPipeline.removeCablesFromBlock(pos);
            }

            CommandHistoryCache.clearCache(pos);
        }

        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + "Hostname: " + hostname + " at: " + getBlockPos().toShortString();
    }
}
