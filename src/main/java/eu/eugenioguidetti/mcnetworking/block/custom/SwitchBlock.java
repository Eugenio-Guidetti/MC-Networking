package eu.eugenioguidetti.mcnetworking.block.custom;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 03/06/2026
 */

import com.mojang.serialization.MapCodec;
import eu.eugenioguidetti.mcnetworking.block.entity.SwitchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 *
 * @author Eugenio Guidetti
 */
public class SwitchBlock extends NetworkingBlock
{
    public SwitchBlock(Properties settings)
    {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return simpleCodec(SwitchBlock::new);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new SwitchBlockEntity(pos, state);
    }
}