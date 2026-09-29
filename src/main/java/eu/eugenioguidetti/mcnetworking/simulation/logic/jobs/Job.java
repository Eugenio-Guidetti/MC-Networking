package eu.eugenioguidetti.mcnetworking.simulation.logic.jobs;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 21/07/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalCache;
import eu.eugenioguidetti.mcnetworking.terminal.TerminalSignal;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.NonNull;

import static eu.eugenioguidetti.mcnetworking.GlobalConstants.MAX_JOBS;

/**
 *
 * @author Eugenio Guidetti
 */
public abstract class Job
{
    protected volatile TerminalSignal receivedSignal = null;
    protected int jobId = -1;
    private volatile boolean running = false;

    protected final NetworkingBlockEntity netEntity;
    protected ConsoleSession session;

    protected Job(NetworkingBlockEntity netEntity)
    {
        this.netEntity = netEntity;
    }

    /**
     * @return true se il job è completato o terminato
     */
    public final boolean tick()
    {
        if (receivedSignal == TerminalSignal.SIGINT || receivedSignal == TerminalSignal.SIGTERM)
        {
            terminate();
            return true;
        }

        if (running && internalTick())
        {
            terminate();
            return true;
        }

        return false;
    }

    private void terminate()
    {
        running = false;
        onTerminate();
    }

    protected abstract boolean internalTick();

    public void handleSignal(@NonNull TerminalSignal signal)
    {
        this.receivedSignal = signal;
    }

    protected void onStart()
    {
    }

    protected void onTerminate()
    {
    }

    public final void setId(int jobId)
    {
        if (jobId <= 0 || jobId > MAX_JOBS)
        {
            throw new IllegalStateException("jobId invalido");
        }
        if (this.jobId != -1)
        {
            throw new IllegalStateException("jobId già assegnato");
        }

        this.running = true;
        this.jobId = jobId;

        if (this.netEntity.getLevel() instanceof ServerLevel)
        {
            this.session = TerminalCache.getOrCreateSession(this.netEntity).session();
        }

        onStart();
    }

    public int getJobId()
    {
        return jobId;
    }

    public boolean isRunning()
    {
        return running;
    }

    public TerminalSignal getReceivedSignal()
    {
        return receivedSignal;
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + ": " + "running=" + running + ", jobId=" + jobId + "\n";
    }
}