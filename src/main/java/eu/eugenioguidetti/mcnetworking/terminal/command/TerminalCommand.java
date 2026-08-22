package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 07/06/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import org.jspecify.annotations.NonNull;

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
    void execute(@NonNull ConsoleSession session, String @NonNull [] args);

    // Indica in quali modalità questo comando è valido e su quali NetworkingBlockEntity
    boolean canRunCommand(@NonNull ConsoleSession session);

    String getDescription(@NonNull ConsoleSession session);
}