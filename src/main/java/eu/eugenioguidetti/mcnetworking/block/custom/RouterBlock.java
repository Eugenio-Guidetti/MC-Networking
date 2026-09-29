package eu.eugenioguidetti.mcnetworking.block.custom;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 03/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.RouterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public class RouterBlock extends NetworkingBlock
{
    public RouterBlock(Properties settings)
    {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state)
    {
        return new RouterBlockEntity(pos, state);
    }
}