package eu.eugenioguidetti.mcnetworking.block.entity;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 03/06/2026
 */

import eu.eugenioguidetti.mcnetworking.client.rendering.CablesRenderPipeline;
import eu.eugenioguidetti.mcnetworking.item.CableType;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkReceiver;
import eu.eugenioguidetti.mcnetworking.simulation.protocol.EthernetFrame;
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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Eugenio Guidetti
 */
public abstract class NetworkingBlockEntity extends BlockEntity implements NetworkReceiver
{
    protected final Map<Direction, NetworkInterface> nics = new HashMap<>();

    public NetworkingBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState)
    {
        super(type, worldPosition, blockState);
    }


    @Override
    public NetworkInterface getInterface(Direction clickedFace)
    {
        return nics.get(clickedFace);
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


    protected void floodFrame(@NotNull EthernetFrame frame, Direction from)
    {
        if (this.level == null || this.level.isClientSide())
        {
            return;
        }

        for (Direction to : nics.keySet())
        {
            if (to.equals(from))
            {
                continue;
            }

            NetworkInterface sendingNic = nics.get(to);
            sendingNic.sendPacket(frame.copy());
        }
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


    // --- Salvataggio/caricamento dati blocco in NBT ---

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);

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
    public void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);

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


    // Blocco distrutto
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state)
    {
        if (this.level != null)
        {
            BlockEntity blockEntity = level.getBlockEntity(pos);

            if (blockEntity instanceof NetworkingBlockEntity netEntity)
            {
                netEntity.disconnectAll();

                if (level.isClientSide())
                {
                    CablesRenderPipeline.removeCablesFromBlock(pos);
                }
            }

            super.preRemoveSideEffects(pos, state);
        }

    }
}
