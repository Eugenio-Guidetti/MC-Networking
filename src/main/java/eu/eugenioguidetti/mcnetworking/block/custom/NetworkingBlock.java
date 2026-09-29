package eu.eugenioguidetti.mcnetworking.block.custom;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 04/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.item.ModItems;
import eu.eugenioguidetti.mcnetworking.networking.packet.OpenTerminalS2CPayload;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkReceiver;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalCache;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public abstract class NetworkingBlock extends BaseEntityBlock
{
    protected NetworkingBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NonNull Level level,
                                                                  @NonNull BlockState state,
                                                                  @NonNull BlockEntityType<T> type)
    {
        if (level.isClientSide())
        {
            return (lvl, pos, st, be) -> NetworkingBlockEntity.clientTick(lvl, pos, st, (NetworkingBlockEntity) be);
        }
        else
        {
            return (lvl, pos, st, be) -> NetworkingBlockEntity.serverTick(lvl, pos, st, (NetworkingBlockEntity) be);
        }
    }

    @Override
    public @NonNull RenderShape getRenderShape(@NonNull BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    protected @NonNull InteractionResult useItemOn(@NonNull ItemStack itemStack,
                                                   @NonNull BlockState state,
                                                   @NonNull Level level,
                                                   @NonNull BlockPos pos,
                                                   @NonNull Player player,
                                                   @NonNull InteractionHand hand,
                                                   @NonNull BlockHitResult hitResult)
    {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer))
        {
            return InteractionResult.SUCCESS;
        }

        if (itemStack.getItem().equals(ModItems.SCISSORS))
        {
            if (!(level.getBlockEntity(pos) instanceof NetworkReceiver receiver))
            {
                return InteractionResult.PASS;
            }

            Direction face = hitResult.getDirection();

            receiver.disconnectPhysical(face);

            return InteractionResult.CONSUME;
        }

        return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
    }

    // Apertura UI terminale
    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state,
                                                        @NonNull Level level,
                                                        @NonNull BlockPos pos,
                                                        @NonNull Player player,
                                                        @NonNull BlockHitResult hitResult)
    {
        if (!player.getMainHandItem().isEmpty())
        {
            return InteractionResult.PASS;
        }

        // Il client qua non fa niente. Apre la UI quando glie lo dice il server
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer))
        {
            return InteractionResult.SUCCESS;
        }

        TerminalCache.CacheValue cached = TerminalCache.getOrCreateSession((ServerLevel) level, pos);

        // Il server dice al client di aprire l'interfaccia terminalScreen
        ServerPlayNetworking.send(serverPlayer,
                                  new OpenTerminalS2CPayload(cached.history(),
                                                             cached.session().getDevice().getForegroundJob().isEmpty(),
                                                             cached.session().getPrompt(),
                                                             GlobalPos.of(level.dimension(), pos)));
        return InteractionResult.CONSUME;
    }
}
