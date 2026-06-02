package eu.eugenioguidetti.mcnetworking.block.entity;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 25/05/2026
 */

import eu.eugenioguidetti.mcnetworking.MCNetworking;
import eu.eugenioguidetti.mcnetworking.block.custom.HostBlock;
import eu.eugenioguidetti.mcnetworking.block.registry.ModBlockEntities;
import eu.eugenioguidetti.mcnetworking.client.rendering.CablesRenderPipeline;
import eu.eugenioguidetti.mcnetworking.item.CableType;
import eu.eugenioguidetti.mcnetworking.item.ConnectorType;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkReceiver;
import eu.eugenioguidetti.mcnetworking.simulation.SimPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Eugenio Guidetti
 */
public class HostBlockEntity extends BlockEntity implements NetworkReceiver
{
    private final Map<Direction, NetworkInterface> nics = new EnumMap<>(Direction.class);

    public HostBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.HOST_BLOCK_ENTITY, pos, state);

        Direction facing = state.getValue(HostBlock.HORIZONTAL_FACING);

        nics.put(facing, new NetworkInterface(pos, facing, ConnectorType.RJ45));
    }

    public NetworkInterface getInterface(Direction face)
    {
        return nics.get(face);
    }

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

        this.sync();
    }

    private void disconnectNic(NetworkInterface nic)
    {
        if (!nic.isConnected())
        {
            return;
        }

        BlockPos targetPos = nic.getConnectedTargetPos();
        Direction targetFace = nic.getConnectedTargetFace();

        CableType cableType = nic.getConnectedCableType();
        ItemStack dropStack = new ItemStack(cableType.getAsItem());

        // Facciamo cadere il cavo al centro del blocco Host che si sta disconnettendo
        Containers.dropItemStack(this.level,
                                 this.worldPosition.getX() + 0.5,
                                 this.worldPosition.getY() + 0.5,
                                 this.worldPosition.getZ() + 0.5,
                                 dropStack);


        BlockEntity targetEntity = this.level.getBlockEntity(targetPos);
        if (targetEntity instanceof NetworkReceiver receiver)
        {
            NetworkInterface remoteNic = receiver.getInterface(targetFace);
            if (remoteNic != null)
            {
                remoteNic.disconnect();
                receiver.sync();
            }
        }

        nic.disconnect();
    }


    @Override
    public void receivePacket(SimPacket packet, Direction from)
    {
        if (this.level == null || this.level.isClientSide())
        {
            return;
        }

        // Per ora alla ricezione di un pacchetto, questo viene stampato in chat
        String message = String.format("[Host at %d, %d, %d] Ricevuto Pacchetto!\nPayload: '%s'",
                                       this.worldPosition.getX(),
                                       this.worldPosition.getY(),
                                       this.worldPosition.getZ(),
                                       packet.payload());
        Objects.requireNonNull(this.level.getServer()).getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }


    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);

        // Creiamo una sezione principale chiamata "NetworkInterfaces"
        ValueOutput nicsOutput = output.child("NetworkInterfaces");

        // Salviamo ciascuna interfaccia nel proprio sotto-ramo
        for (Map.Entry<Direction, NetworkInterface> entry : this.nics.entrySet())
        {
            Direction face = entry.getKey();
            NetworkInterface nic = entry.getValue();

            // Es: crea un child chiamato "UP" e passalo all'interfaccia
            ValueOutput faceOutput = nicsOutput.child(face.name());
            nic.save(faceOutput);
        }
    }

    @Override
    public void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);

        // child() cerca la sezione "NetworkInterfaces".
        // Se la trova (ifPresent), esegue il blocco di codice al suo interno.
        ValueInput nicsInput = input.child("NetworkInterfaces").orElse(null);

        if (nicsInput == null)
        {
            return;
        }

        for (Map.Entry<Direction, NetworkInterface> entry : this.nics.entrySet())
        {
            Direction face = entry.getKey();
            NetworkInterface nic = entry.getValue();

            // Cerchiamo il sotto-ramo per questa specifica faccia (es. "UP").
            // Se esiste, passiamo l'input al metodo load della nostra interfaccia!
            nicsInput.child(face.name()).ifPresent(_input -> nic.load(_input, face));
        }
    }

    // Metodo chiamato dal Blocco quando sente la Redstone
    public void triggerSendPacket()
    {
        if (this.level == null || this.level.isClientSide())
        {
            return;
        }


        // Invia il pacchetto alle interfacce
        for (NetworkInterface nic : this.nics.values())
        {
            if (!nic.isConnected())
            {
                continue;
            }

            String payload = String.format("""
                                                   PING§7
                                                   Sender nic:
                                                      MAC: %s
                                                      pos: %s
                                                      dir: %s
                                                      connType: %s
                                                   Receiver nic:
                                                      MAC: %s
                                                      pos: %s
                                                      dir: %s
                                                   CableType: %s""",
                                           nic.getMacAddress(),
                                           nic.getPos().toShortString(),
                                           nic.getDirection(),
                                           nic.getConnectorType(),
                                           nic.getConnectedMacAddress(),
                                           nic.getConnectedTargetPos().toShortString(),
                                           nic.getConnectedTargetFace(),
                                           nic.getConnectedCableType().name());

            SimPacket packet = new SimPacket(payload);
            MCNetworking.LOGGER.info("Sending Host Packet: " + packet);

            // TODO: spawn particelle

            nic.sendPacket(packet);
        }
    }

    // Questo metodo verrà chiamato da Minecraft 20 volte al secondo
    public static void tick(Level level, BlockPos pos, BlockState state, HostBlockEntity entity)
    {
        if (level.isClientSide())
        {
            return;
        }

        for (NetworkInterface nic : entity.nics.values())
        {
            nic.tick(level);
        }

        for (NetworkInterface nic : entity.nics.values())
        {
            if (nic.isConnected())
            {
                CablesRenderPipeline.addCable(nic.getMacAddress(),
                                              nic.getPos(),
                                              nic.getDirection(),
                                              nic.getConnectedMacAddress(),
                                              nic.getConnectedTargetPos(),
                                              nic.getConnectedTargetFace(),
                                              nic.getConnectedCableType());
            }
            else
            {
                CablesRenderPipeline.removeCable(nic.getMacAddress());
            }
        }
    }

    // Aggiornamento server->client dello stato delle block entity
    // Definisce il pacchetto che verrà spedito al client
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // Prepara i dati da inserire nel pacchetto (sfruttiamo il salvataggio che hai già fatto)
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        // Chiama l'implementazione base che, nelle ultime versioni, si aggancia automaticamente a saveAdditional e alla tua ValueOutput pipeline.
        //return super.getUpdateTag(registries);
        return this.saveCustomOnly(registries);
    }

    // Sincronizza i dati con i client
    @Override
    public void sync()
    {
        if (this.level != null && !this.level.isClientSide())
        {
            // Segna il chunk come "sporco" così Minecraft lo salverà su disco
            this.setChanged();
            // Invia il pacchetto di aggiornamento a tutti i giocatori che stanno guardando questo blocco
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }


    // Blocco distrutto
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state)
    {
        if (this.level != null)
        {
            this.disconnectAll();
        }

        super.preRemoveSideEffects(pos, state);
    }
}
