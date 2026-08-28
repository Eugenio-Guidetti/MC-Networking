package eu.eugenioguidetti.mcnetworking.terminal.command;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 27/08/2026
 */

import eu.eugenioguidetti.mcnetworking.terminal.ConsoleSession;
import org.jspecify.annotations.NonNull;

/**
 *
 * @author Eugenio Guidetti
 */
public interface UndoableTerminalCommand extends TerminalCommand
{
    void undo(@NonNull ConsoleSession session, String @NonNull [] args);
}
