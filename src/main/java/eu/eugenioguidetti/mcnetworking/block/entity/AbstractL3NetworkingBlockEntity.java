package eu.eugenioguidetti.mcnetworking.block.entity;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 01/08/2026
 */

import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.ArpManager;
import eu.eugenioguidetti.mcnetworking.simulation.logic.jobs.Job;
import eu.eugenioguidetti.mcnetworking.simulation.logic.protocol.IcmpManager;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 *
 * @author Eugenio Guidetti
 */
public abstract class AbstractL3NetworkingBlockEntity extends NetworkingBlockEntity
{
    private int arpManagerJobId = -1;
    protected final IcmpManager icmpManager;

    public AbstractL3NetworkingBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState)
    {
        super(type, worldPosition, blockState);

        this.icmpManager = new IcmpManager(this);
    }

    @Override
    public void setLevel(@NonNull Level level)
    {
        // Avvio i demoni, solo lato server, solo dopo che il mondo è stato inizializzato

        super.setLevel(level);

        if (level.isClientSide())
        {
            return;
        }

        TerminalCache.getOrCreateSession(this);

        if (getArpManager().isEmpty())
        {
            startArpManager();
        }
    }

    public void startArpManager()
    {
        arpManagerJobId = startJob(new ArpManager(this), false);
    }

    public Optional<ArpManager> getArpManager()
    {
        if (arpManagerJobId == -1)
        {
            return Optional.empty();
        }

        Optional<Job> foundJob = getJob(arpManagerJobId);

        if (foundJob.isEmpty())
        {
            arpManagerJobId = -1;
            return Optional.empty();
        }

        return Optional.of((ArpManager) foundJob.get());
    }

    public IcmpManager getIcmpManager()
    {
        return icmpManager;
    }


    @Override
    public int getDeviceLayer()
    {
        return 3;
    }
}
