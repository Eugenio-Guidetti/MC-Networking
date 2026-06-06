package eu.eugenioguidetti.mcnetworking.block.custom;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 04/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

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
}
