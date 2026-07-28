package eu.eugenioguidetti.mcnetworking.terminal;

/*
Nome: Eugenio
Cognome: Guidetti
Data: 07/06/2026
 */

import eu.eugenioguidetti.mcnetworking.block.entity.NetworkingBlockEntity;
import eu.eugenioguidetti.mcnetworking.simulation.NetworkInterface;
import eu.eugenioguidetti.mcnetworking.terminal.packet.TerminalOutputS2CPacket;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 *
 * @author Eugenio Guidetti
 */
public class ConsoleSession
{
    private final BlockPos pos;
    private final ServerLevel level;
    private final GlobalPos globalPos;

    private final NetworkingBlockEntity device;
    private TerminalMode currentMode = TerminalMode.USER_EXEC;

    // Se siamo in (config-if), qui salviamo quale interfaccia stiamo modificando
    private String selectedInterfaceName = null;

    public ConsoleSession(BlockPos pos, ServerLevel level, NetworkingBlockEntity device)
    {
        this.pos = pos;
        this.level = level;
        this.device = device;

        globalPos = GlobalPos.of(level.dimension(), pos);
    }

    public void sendOutput(String output)
    {
        TerminalCache.CacheValue cached = TerminalCache.getOrCreateSession(level, pos);

        TerminalCache.addLine(level, pos, output);

        // 3. Spedisci l'output e il prompt aggiornato indietro ai client
        for (ServerPlayer player : PlayerLookup.around(level, pos, 16))
        {
            ServerPlayNetworking.send(player, new TerminalOutputS2CPacket(output, this.getPrompt(), globalPos));
        }
    }

    public void sendError(String error)
    {
        this.sendOutput("§4Errore: " + error);
    }

    public void sendError(String error, Exception e)
    {
        this.sendOutput("§4Errore: " + error);
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public ServerLevel getLevel()
    {
        return level;
    }

    public NetworkingBlockEntity getDevice()
    {
        return device;
    }

    public TerminalMode getCurrentMode()
    {
        return currentMode;
    }

    public void setCurrentMode(TerminalMode currentMode)
    {
        this.currentMode = currentMode;

        // Invia un output vuoto per aggiornare il prompt
        for (ServerPlayer player : PlayerLookup.level(level))
        {
            GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);

            ServerPlayNetworking.send(player, new TerminalOutputS2CPacket("", this.getPrompt(), globalPos));
        }
    }

    public NetworkInterface getSelectedInterface()
    {
        return device.getInterface(this.selectedInterfaceName);
    }

    public String getSelectedInterfaceName()
    {
        return selectedInterfaceName;
    }

    public void selectInterface(String selectedInterfaceName)
    {
        if (selectedInterfaceName != null && selectedInterfaceName.equals(NetworkInterface.LOOPBACK_NAME))
        {
            throw new IllegalArgumentException("Non puoi selezionare l'interfaccia di loopback");
        }

        this.selectedInterfaceName = selectedInterfaceName;
    }

    public void deselectInterface()
    {
        this.selectedInterfaceName = null;
    }

    public String getPrompt()
    {
        return device.getHostname() + currentMode.getPromptSuffix();
    }

    @Override
    public String toString()
    {
        return "ConsoleSession{" + "device=" + device + ", currentMode=" + currentMode + '}';
    }
}