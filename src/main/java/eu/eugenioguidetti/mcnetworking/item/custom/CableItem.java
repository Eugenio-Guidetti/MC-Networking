package eu.eugenioguidetti.mcnetworking.item.custom;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 26/05/2026
 */

import eu.eugenioguidetti.mcnetworking.GlobalConstants;
import eu.eugenioguidetti.mcnetworking.Utils;
import eu.eugenioguidetti.mcnetworking.component.ModDataComponentTypes;
import eu.eugenioguidetti.mcnetworking.component.PendingConnection;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkReceiver;
import eu.eugenioguidetti.mcnetworking.simulation.models.cables.CableType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

/**
 *
 * @author Eugenio Guidetti
 */
public class CableItem extends Item
{
    private final CableType cableType;

    public CableItem(Properties properties, CableType cableType)
    {
        super(properties);
        this.cableType = cableType;
    }

    public CableItem(Properties properties)
    {
        super(properties);
        this.cableType = CableType.COPPER_STRAIGHT;
    }

    @Override
    public @NonNull InteractionResult useOn(@NonNull UseOnContext context)
    {
        if (context.getLevel().isClientSide())
        {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        ItemStack heldItem = context.getItemInHand();
        PendingConnection pending = heldItem.get(ModDataComponentTypes.PENDING_CONNECTION);

        BlockPos clickedPos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        BlockEntity targetEntity = context.getLevel().getBlockEntity(clickedPos);

        if (player == null)
        {
            return InteractionResult.PASS;
        }

        if (player.isCrouching() && pending != null)
        {
            player.sendSystemMessage(Component.translatable("mcnetworking.cable.cancelled"));

            heldItem.remove(ModDataComponentTypes.PENDING_CONNECTION);
            return InteractionResult.SUCCESS;
        }

        if (!(targetEntity instanceof NetworkReceiver receiver))
        {
            return InteractionResult.PASS;
        }

        NetworkInterface clickedNic = receiver.getInterface(clickedFace);

        if (clickedNic == null)
        {
            player.sendSystemMessage(Component.translatable("mcnetworking.cable.no_interfaces_on_this_side"));
            return InteractionResult.FAIL;
        }

        if (clickedNic.getConnectorType() != cableType.connectorType())
        {
            player.sendSystemMessage(Component.translatable("mcnetworking.cable.wrong_connector"));
            return InteractionResult.FAIL;
        }

        if (clickedNic.isConnected())
        {
            player.sendSystemMessage(Component.translatable("mcnetworking.cable.interface_already_connected"));
            return InteractionResult.FAIL;
        }


        if (pending == null)
        {
            // PRIMO CLIC: Inizia la connessione.
            // Salviamo le coordinate dentro l'oggetto (ItemStack)
            player.sendSystemMessage(Component.literal(String.format(Component
                                                                             .translatable("mcnetworking.cable.link_started_format")
                                                                             .getString(), cableType.name(), clickedPos.toShortString())));

            heldItem.set(ModDataComponentTypes.PENDING_CONNECTION, new PendingConnection(clickedPos, clickedFace));
            return InteractionResult.SUCCESS;
        }


        // SECONDO CLIC: Concludi o annulla
        BlockPos firstPos = pending.pos();
        Direction firstFace = pending.face();

        // Cliccato stesso blocco
        if (clickedPos.equals(firstPos))
        {
            player.sendSystemMessage(Component.translatable("mcnetworking.cable.cancelled"));

            heldItem.remove(ModDataComponentTypes.PENDING_CONNECTION);
            return InteractionResult.SUCCESS;
        }


        BlockEntity firstEntity = context.getLevel().getBlockEntity(firstPos);
        if (!(firstEntity instanceof NetworkReceiver firstReceiver))
        {
            return InteractionResult.FAIL;
        }

        NetworkInterface firstNic = firstReceiver.getInterface(firstFace);

        if (firstNic.isConnected() || clickedNic.isConnected())
        {
            player.sendSystemMessage(Component.translatable("mcnetworking.cable.cancelled.already_connected"));

            heldItem.remove(ModDataComponentTypes.PENDING_CONNECTION);
            return InteractionResult.FAIL;
        }

        if (!isConnectionValid(firstReceiver, receiver, this.cableType))
        {
            player.sendSystemMessage(Component.translatable("mcnetworking.cable.connection_invalid"));

            return InteractionResult.FAIL;
        }

        int cableLengthSquared = Utils.calcDistanceSquared(firstPos, clickedPos);

        if (cableLengthSquared > GlobalConstants.CABLES_MAX_LENGTH_SQUARED)
        {
            player.sendSystemMessage(Component.translatable("mcnetworking.cable.cancelled.too_far"));

            return InteractionResult.FAIL;
        }

        if (!isConnectionPathClear(context.getLevel(), firstPos, firstFace, clickedPos, clickedFace))
        {
            player.sendSystemMessage(Component.translatable("mcnetworking.cable.cancelled.obstructed"));

            return InteractionResult.FAIL;
        }

        double cableLength = Math.sqrt(cableLengthSquared);

        NetworkInterface.PhysicalConnectionData connectionData1 = new NetworkInterface.PhysicalConnectionData(clickedPos,
                                                                                                              receiver.getInterfaceName(
                                                                                                                      clickedFace),
                                                                                                              clickedFace,
                                                                                                              this.cableType,
                                                                                                              cableLength);
        NetworkInterface.PhysicalConnectionData connectionData2 = new NetworkInterface.PhysicalConnectionData(firstPos,
                                                                                                              firstReceiver.getInterfaceName(
                                                                                                                      firstFace),
                                                                                                              firstFace,
                                                                                                              this.cableType,
                                                                                                              cableLength);

        firstNic.connect(connectionData1);
        clickedNic.connect(connectionData2);


        firstReceiver.sync();
        receiver.sync();

        // Resetto il cavo rimuovendo il componente
        heldItem.remove(ModDataComponentTypes.PENDING_CONNECTION);
        if (!player.isCreative())
        {
            heldItem.shrink(1);
        }


        player.sendSystemMessage(Component.literal(String.format(Component.translatable("mcnetworking.cable.link_ended_format").getString(),
                                                                 cableType.name())));
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NonNull InteractionResult use(@NonNull Level level, @NonNull Player player, @NonNull InteractionHand hand)
    {
        if (level.isClientSide())
        {
            return InteractionResult.SUCCESS;
        }

        ItemStack heldItem = player.getItemInHand(hand);
        PendingConnection pending = heldItem.get(ModDataComponentTypes.PENDING_CONNECTION);

        // Se il giocatore fa Shift + Clic destro nell'aria ed ha un cavo attivo, lo resetta
        if (!player.isCrouching() || pending == null)
        {
            return InteractionResult.PASS;
        }

        heldItem.remove(ModDataComponentTypes.PENDING_CONNECTION);

        player.sendSystemMessage(Component.translatable("mcnetworking.cable.cancelled"));

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(@NonNull ItemStack itemStack,
                                @NonNull TooltipContext context,
                                @NonNull TooltipDisplay display,
                                @NonNull Consumer<Component> builder,
                                @NonNull TooltipFlag tooltipFlag)
    {
        PendingConnection pendingConnection = itemStack.get(ModDataComponentTypes.PENDING_CONNECTION);

        if (pendingConnection == null)
        {
            builder.accept(Component.translatable("tooltip.mcnetworking.cable"));
        }
        else
        {
            builder.accept(Component.translatable("tooltip.mcnetworking.cable.pending_connection"));
            builder.accept(Component.translatable("tooltip.mcnetworking.cable.pending_connection.cancel"));
            String literal = String.format(Component
                                                   .translatable("tooltip.mcnetworking.cable.pending_connection.first_interface")
                                                   .getString(), pendingConnection.pos().toShortString(), pendingConnection.face());

            builder.accept(Component.literal(literal));
        }

        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }

    // Effetto "incantato" all'item se c'è una connessione in corso
    @Override
    public boolean isFoil(@NonNull ItemStack stack)
    {
        return stack.has(ModDataComponentTypes.PENDING_CONNECTION) || super.isFoil(stack);
    }

    /**
     * Determina se il dispositivo agisce come MDI-X (Switch/Hub).
     * Router e PC agiscono come MDI standard.
     */
    private boolean isMdiX(@NonNull NetworkReceiver device)
    {
        return device.getDeviceLayer() <= 2;
    }

    private boolean isConnectionValid(NetworkReceiver firstReceiver, NetworkReceiver receiver, CableType cableType)
    {
        // Se è un cavo in fibra o console, la logica MDI/MDI-X non si applica
        if (cableType != CableType.COPPER_STRAIGHT && cableType != CableType.COPPER_CROSSOVER)
        {
            return true;
        }

        boolean firstIsMdiX = isMdiX(firstReceiver);
        boolean secondIsMdiX = isMdiX(receiver);

        // Se i dispositivi sono dello stesso tipo logico (es. PC-PC o Switch-Switch)
        if (firstIsMdiX == secondIsMdiX)
        {
            return cableType == CableType.COPPER_CROSSOVER;
        }
        // Se i dispositivi sono di tipo diverso (es. PC-Switch)
        else
        {
            return cableType == CableType.COPPER_STRAIGHT;
        }
    }

    private boolean isConnectionPathClear(@NonNull Level level,
                                          BlockPos firstPos,
                                          Direction firstFace,
                                          BlockPos clickedPos,
                                          Direction clickedFace)
    {
        Vec3 start = Utils.getInterfaceCenterPoint(firstPos, firstFace);
        Vec3 end = Utils.getInterfaceCenterPoint(clickedPos, clickedFace);

        // ignora blocchi "trasparenti" e fluidi
        ClipContext context = new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (CollisionContext) null);

        BlockHitResult hit = level.clip(context);

        // Se il raggio non colpisce nulla, il percorso è completamente libero
        if (hit.getType() == HitResult.Type.MISS)
        {
            return true;
        }

        // Se colpisce un blocco, verifichiamo che sia esattamente il blocco di destinazione
        // (previene falsi negativi dovuti ad approssimazioni matematiche all'arrivo)
        return hit.getBlockPos().equals(clickedPos);
    }
}