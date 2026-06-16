package eu.eugenioguidetti.mcnetworking.block.custom;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 04/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalCache;
import eu.eugenioguidetti.mcnetworking.terminal.packet.OpenTerminalS2CPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

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
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
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
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        if (!player.getMainHandItem().isEmpty())
        {
            return InteractionResult.PASS;
        }

        // Apri terminale solo se il giocatore ha in mano un item specifico
        /*
        if (!(player.getMainHandItem().getItem() instanceof CableItem))
        {
            return InteractionResult.PASS;
        }
        */

        // Il server dice al client di aprire l'interfaccia
        // Il client qua non fa niente. Apre la UI quando glie lo dice il server
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer))
        {
            return InteractionResult.SUCCESS;
        }

        TerminalCache.CacheValue cached = TerminalCache.getOrCreateSession(level, pos);

        ServerPlayNetworking.send(serverPlayer, new OpenTerminalS2CPacket(pos, cached.history(), cached.session().getPrompt()));
        return InteractionResult.CONSUME;
    }
}
