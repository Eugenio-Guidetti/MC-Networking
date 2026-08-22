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

import static eu.eugenioguidetti.mcnetworking.GlobalConstants.MAX_JOBS;

/**
 *
 * @author Eugenio Guidetti
 */
public abstract class Job
{
    protected boolean forceTerminate = false;

    protected int jobId = -1;

    protected final NetworkingBlockEntity netEntity;
    protected final ConsoleSession session;

    protected Job(NetworkingBlockEntity netEntity)
    {
        this.netEntity = netEntity;
        this.session = TerminalCache.getOrCreateSession(netEntity).session();
    }

    /**
     * @return true se il job è completato
     */
    public final boolean tick()
    {
        if (forceTerminate || (jobId != -1 && internalTick()))
        {
            onTerminate();
            return true;
        }

        return false;
    }

    protected abstract boolean internalTick();

    public void handleSignal(TerminalSignal signal)
    {
        if (signal == TerminalSignal.SIGINT)
        {
            this.forceTerminate = true;
        }
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

        this.jobId = jobId;

        onStart();
    }
}
