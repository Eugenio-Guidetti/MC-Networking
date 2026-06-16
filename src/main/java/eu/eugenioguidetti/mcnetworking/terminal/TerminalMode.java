package eu.eugenioguidetti.mcnetworking.terminal;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 07/06/2026
 */

/**
 *
 * @author Eugenio Guidetti
 */
public enum TerminalMode
{
    USER_EXEC("> "),
    PRIV_EXEC("# "),
    GLOBAL_CONFIG("(config)# "),
    INTERFACE_CONFIG("(config-if)# ");

    private final String promptSuffix;

    TerminalMode(String promptSuffix)
    {
        this.promptSuffix = promptSuffix;
    }

    public String getPromptSuffix()
    {
        return promptSuffix;
    }
}
