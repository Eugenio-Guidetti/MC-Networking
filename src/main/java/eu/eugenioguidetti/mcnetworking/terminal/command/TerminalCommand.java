package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 07/06/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;

/**
 *
 * @author Eugenio Guidetti
 */
public interface TerminalCommand
{
    /**
     * @param session La sessione attuale
     * @param args    Gli argomenti del comando. args[0] è il nome del comando
     */
    void execute(ConsoleSession session, String[] args);

    // Indica in quali modalità questo comando è valido e su quali NetworkingBlockEntity
    boolean canRunCommand(ConsoleSession session);

    String getDescription(ConsoleSession session);
}